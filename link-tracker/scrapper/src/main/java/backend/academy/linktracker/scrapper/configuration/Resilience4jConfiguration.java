package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.properties.CircuitBreakerConfigProperties;
import backend.academy.linktracker.scrapper.properties.RateLimiterConfigProperties;
import backend.academy.linktracker.scrapper.properties.RetryConfigProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.HashSet;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpServerErrorException;

@Configuration
@EnableConfigurationProperties({
    RetryConfigProperties.class,
    CircuitBreakerConfigProperties.class,
    RateLimiterConfigProperties.class
})
public class Resilience4jConfiguration {

    @Bean
    public RetryRegistry retryRegistry(RetryConfigProperties props) {
        var retryable = new HashSet<>(props.getRetryableStatuses());
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(props.getMaxAttempts())
                .waitDuration(props.getWaitDuration())
                .retryOnException(ex -> {
                    if (ex instanceof HttpServerErrorException e) {
                        return retryable.contains(e.getStatusCode().value());
                    }
                    return false;
                })
                .build();
        return RetryRegistry.of(config);
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfigProperties props) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(props.getSlidingWindowSize())
                .failureRateThreshold(props.getFailureRateThreshold())
                .waitDurationInOpenState(props.getWaitDurationInOpenState())
                .permittedNumberOfCallsInHalfOpenState(props.getPermittedCallsInHalfOpenState())
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();
        return CircuitBreakerRegistry.of(config);
    }

    @Bean
    public RateLimiterRegistry rateLimiterRegistry(RateLimiterConfigProperties props) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(props.getLimitForPeriod())
                .limitRefreshPeriod(props.getLimitRefreshPeriod())
                .timeoutDuration(props.getTimeoutDuration())
                .build();
        return RateLimiterRegistry.of(config);
    }
}
