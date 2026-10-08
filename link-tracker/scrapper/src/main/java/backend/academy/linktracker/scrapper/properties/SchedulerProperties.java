package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.scheduler")
public class SchedulerProperties {

    private long delay = 60_000;

    @Min(50)
    @Max(500)
    private int batchSize = 100;

    @Min(1)
    private int threadCount = 4;
}
