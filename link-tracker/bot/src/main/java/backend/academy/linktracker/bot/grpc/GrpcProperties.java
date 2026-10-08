package backend.academy.linktracker.bot.grpc;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.grpc")
public class GrpcProperties {
    private int port = 8090;
}
