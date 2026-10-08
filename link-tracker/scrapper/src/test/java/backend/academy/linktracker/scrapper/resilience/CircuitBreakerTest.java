package backend.academy.linktracker.scrapper.resilience;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.linktracker.scrapper.client.GithubClient;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@EnableWireMock
class CircuitBreakerTest {

    private static final int WINDOW_SIZE = 5;
    private static final float FAILURE_RATE = 60f;
    private static final int HALF_OPEN_CALLS = 2;
    private static final Duration WAIT_OPEN = Duration.ofMillis(300);

    @InjectWireMock
    private WireMockServer wireMock;

    private GithubClient rawClient;
    private CircuitBreaker cb;

    @BeforeEach
    void setUp() {
        rawClient = new GithubClient(RestClient.builder()
                .baseUrl("http://localhost:" + wireMock.port())
                .build());

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(WINDOW_SIZE)
                .failureRateThreshold(FAILURE_RATE)
                .waitDurationInOpenState(WAIT_OPEN)
                .permittedNumberOfCallsInHalfOpenState(HALF_OPEN_CALLS)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();
        cb = CircuitBreakerRegistry.of(config).circuitBreaker("test");
    }

    private Supplier<Object> decorated() {
        return CircuitBreaker.decorateSupplier(cb, () -> rawClient.fetchLatestUpdates("o", "r", Instant.EPOCH));
    }

    @Test
    void tc41_circuitOpensAfterFailureThreshold_thenFastFails() {
        stubFor(get(urlPathMatching("/repos/.*")).willReturn(aResponse().withStatus(500)));

        for (int i = 0; i < WINDOW_SIZE; i++) {
            try {
                decorated().get();
            } catch (Exception ignored) {
            }
        }

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        long start = System.currentTimeMillis();
        assertThatThrownBy(() -> decorated().get()).isInstanceOf(CallNotPermittedException.class);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).isLessThan(100);
        verify(WINDOW_SIZE, getRequestedFor(urlPathMatching("/repos/.*")));
    }

    @Test
    void tc42_halfOpenToClosedOnSuccess() throws InterruptedException {
        stubFor(get(urlPathMatching("/repos/.*")).willReturn(aResponse().withStatus(500)));

        for (int i = 0; i < WINDOW_SIZE; i++) {
            try {
                decorated().get();
            } catch (Exception ignored) {
            }
        }

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        Thread.sleep(WAIT_OPEN.toMillis() + 100);

        stubFor(get(urlPathMatching("/repos/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("[]")));

        for (int i = 0; i < HALF_OPEN_CALLS; i++) {
            decorated().get();
        }

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void tc43_halfOpenToOpenOnFailure() throws InterruptedException {
        stubFor(get(urlPathMatching("/repos/.*")).willReturn(aResponse().withStatus(500)));

        for (int i = 0; i < WINDOW_SIZE; i++) {
            try {
                decorated().get();
            } catch (Exception ignored) {
            }
        }

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        Thread.sleep(WAIT_OPEN.toMillis() + 100);

        for (int i = 0; i < HALF_OPEN_CALLS; i++) {
            try {
                decorated().get();
            } catch (Exception ignored) {
            }
        }

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }
}
