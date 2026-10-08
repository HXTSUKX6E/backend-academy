package backend.academy.linktracker.scrapper.resilience;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.linktracker.scrapper.client.GithubClient;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@EnableWireMock
class TimeoutTest {

    private static final int SERVER_DELAY_MS = 5_000;
    private static final Duration CLIENT_TIMEOUT = Duration.ofSeconds(1);

    @InjectWireMock
    private WireMockServer wireMock;

    private GithubClient client;

    @BeforeEach
    void setUp() {
        stubFor(get(urlPathMatching("/repos/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(SERVER_DELAY_MS)
                        .withBody("[]")));

        var httpClient = HttpClient.newBuilder().connectTimeout(CLIENT_TIMEOUT).build();
        var factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(CLIENT_TIMEOUT);

        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + wireMock.port())
                .requestFactory(factory)
                .build();
        client = new GithubClient(restClient);
    }

    @Test
    void tc11_requestTimesOutBeforeServerResponds() {
        long start = System.currentTimeMillis();

        assertThatThrownBy(() -> client.fetchLatestUpdates("owner", "repo", Instant.EPOCH))
                .isInstanceOf(ResourceAccessException.class);

        long elapsed = System.currentTimeMillis() - start;
        assertThat(elapsed).isLessThan(SERVER_DELAY_MS);
    }

    private static org.assertj.core.api.AbstractLongAssert<?> assertThat(long value) {
        return org.assertj.core.api.Assertions.assertThat(value);
    }
}
