package backend.academy.linktracker.scrapper.properties;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.rate-limiter")
public class RateLimiterConfigProperties {

    private int limitForPeriod = 20;
    private Duration limitRefreshPeriod = Duration.ofSeconds(1);
    private Duration timeoutDuration = Duration.ZERO;
}
