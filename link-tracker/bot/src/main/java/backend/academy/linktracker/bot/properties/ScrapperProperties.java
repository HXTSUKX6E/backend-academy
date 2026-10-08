package backend.academy.linktracker.bot.properties;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.scrapper")
public class ScrapperProperties {
    @NotEmpty
    @URL
    private String baseUrl = "http://localhost:8081";
}
