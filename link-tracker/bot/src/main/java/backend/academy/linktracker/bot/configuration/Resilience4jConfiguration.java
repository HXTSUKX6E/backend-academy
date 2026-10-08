package backend.academy.linktracker.bot.configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpServerErrorException;

@Configuration
public class Resilience4jConfiguration {

    @Value("${app.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${app.retry.wait-duration:500ms}")
    private Duration waitDuration;

    @Value("${app.circuit-breaker.sliding-window-size:10}")
    private int slidingWindowSize;

    @Value("${app.circuit-breaker.failure-rate-threshold:50}")
    private float failureRateThreshold;

    @Value("${app.circuit-breaker.wait-duration-in-open-state:10s}")
    private Duration waitDurationInOpenState;

    @Value("${app.circuit-breaker.permitted-calls-in-half-open-state:3}")
    private int permittedCallsInHalfOpenState;

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .waitDuration(waitDuration)
                .retryOnException(ex -> ex instanceof HttpServerErrorException)
                .build();
        return RetryRegistry.of(config);
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(slidingWindowSize)
                .failureRateThreshold(failureRateThreshold)
                .waitDurationInOpenState(waitDurationInOpenState)
                .permittedNumberOfCallsInHalfOpenState(permittedCallsInHalfOpenState)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();
        return CircuitBreakerRegistry.of(config);
    }
}
