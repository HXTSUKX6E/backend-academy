package backend.academy.linktracker.scrapper.properties;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.circuit-breaker")
public class CircuitBreakerConfigProperties {

    private int slidingWindowSize = 10;
    private float failureRateThreshold = 50f;
    private Duration waitDurationInOpenState = Duration.ofSeconds(10);
    private int permittedCallsInHalfOpenState = 3;
}
