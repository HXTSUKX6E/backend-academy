package backend.academy.linktracker.scrapper.resilience;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.TestcontainersConfiguration;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

@Tag("integration")
@SpringBootTest
@Import({TestcontainersConfiguration.class, RateLimitingTest.TestRateLimiterConfig.class})
@AutoConfigureMockMvc
class RateLimitingTest {

    private static final int LIMIT = 3;

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class TestRateLimiterConfig {
        @Bean
        @Primary
        RateLimiterRegistry testRateLimiterRegistry() {
            RateLimiterConfig config = RateLimiterConfig.custom()
                    .limitForPeriod(LIMIT)
                    .limitRefreshPeriod(Duration.ofMinutes(1))
                    .timeoutDuration(Duration.ZERO)
                    .build();
            return RateLimiterRegistry.of(config);
        }
    }

    @Test
    void tc31_requestsWithinLimitSucceed_excessiveRequestsGet429() throws Exception {
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger tooManyCount = new AtomicInteger();

        int total = LIMIT + 3;
        for (int i = 0; i < total; i++) {
            int status = mockMvc.perform(get("/links").header("Tg-Chat-Id", "999"))
                    .andReturn()
                    .getResponse()
                    .getStatus();

            if (status == 429) {
                tooManyCount.incrementAndGet();
            } else {
                successCount.incrementAndGet();
            }
        }

        org.assertj.core.api.Assertions.assertThat(successCount.get()).isEqualTo(LIMIT);
        org.assertj.core.api.Assertions.assertThat(tooManyCount.get()).isEqualTo(total - LIMIT);
    }
}
