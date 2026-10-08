package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.bot")
public class BotProperties {
    @NotEmpty
    @URL
    private String baseUrl = "http://localhost:8080";

    private String protocol = "rest";
    private String grpcHost = "localhost";
    private int grpcPort = 8090;
}
