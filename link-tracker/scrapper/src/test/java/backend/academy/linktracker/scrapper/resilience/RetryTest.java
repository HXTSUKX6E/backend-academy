package backend.academy.linktracker.scrapper.resilience;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.linktracker.scrapper.client.GithubClient;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@EnableWireMock
class RetryTest {

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration WAIT = Duration.ofMillis(100);
    private static final Set<Integer> RETRYABLE = Set.of(500, 502, 503, 504);

    @InjectWireMock
    private WireMockServer wireMock;

    private GithubClient rawClient;
    private Retry retry;

    @BeforeEach
    void setUp() {
        rawClient = new GithubClient(RestClient.builder()
                .baseUrl("http://localhost:" + wireMock.port())
                .build());

        RetryConfig config = RetryConfig.custom()
                .maxAttempts(MAX_ATTEMPTS)
                .waitDuration(WAIT)
                .retryOnException(ex -> {
                    if (ex instanceof HttpServerErrorException e) {
                        return RETRYABLE.contains(e.getStatusCode().value());
                    }
                    return false;
                })
                .build();
        retry = RetryRegistry.of(config).retry("test");
    }

    @Test
    void tc21_retryOn5xx_eventuallySucceeds() {
        stubFor(get(urlPathMatching("/repos/.*"))
                .inScenario("retry-5xx")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("attempt-2"));

        stubFor(get(urlPathMatching("/repos/.*"))
                .inScenario("retry-5xx")
                .whenScenarioStateIs("attempt-2")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("attempt-3"));

        stubFor(get(urlPathMatching("/repos/.*"))
                .inScenario("retry-5xx")
                .whenScenarioStateIs("attempt-3")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("[]")));

        List<?> result = Retry.decorateSupplier(retry, () -> rawClient.fetchLatestUpdates("o", "r", Instant.EPOCH))
                .get();

        assertThat(result).isEmpty();
        verify(MAX_ATTEMPTS, getRequestedFor(urlPathMatching("/repos/.*")));
    }

    @Test
    void tc22_noRetryOn4xx() {
        stubFor(get(urlPathMatching("/repos/.*")).willReturn(aResponse().withStatus(400)));

        assertThatThrownBy(
                        () -> Retry.decorateSupplier(retry, () -> rawClient.fetchLatestUpdates("o", "r", Instant.EPOCH))
                                .get())
                .isInstanceOf(Exception.class);

        verify(1, getRequestedFor(urlPathMatching("/repos/.*")));
    }

    @Test
    void tc23_constantBackoffInterval() {
        stubFor(get(urlPathMatching("/repos/.*"))
                .inScenario("backoff")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("attempt-2"));
        stubFor(get(urlPathMatching("/repos/.*"))
                .inScenario("backoff")
                .whenScenarioStateIs("attempt-2")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("attempt-3"));
        stubFor(get(urlPathMatching("/repos/.*"))
                .inScenario("backoff")
                .whenScenarioStateIs("attempt-3")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("[]")));

        long start = System.currentTimeMillis();
        Retry.decorateSupplier(retry, () -> rawClient.fetchLatestUpdates("o", "r", Instant.EPOCH))
                .get();
        long elapsed = System.currentTimeMillis() - start;

        long expectedMin = WAIT.toMillis() * 2;
        assertThat(elapsed).isGreaterThanOrEqualTo(expectedMin);
        assertThat(elapsed).isLessThan(WAIT.toMillis() * 5);
    }
}
