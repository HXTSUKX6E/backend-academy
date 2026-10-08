package backend.academy.linktracker.scrapper.properties;

import java.time.Duration;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.retry")
public class RetryConfigProperties {

    private int maxAttempts = 3;
    private Duration waitDuration = Duration.ofMillis(500);
    private List<Integer> retryableStatuses = List.of(500, 502, 503, 504);
}
