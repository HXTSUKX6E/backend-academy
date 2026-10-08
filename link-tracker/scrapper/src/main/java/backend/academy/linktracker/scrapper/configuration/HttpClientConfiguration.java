package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.properties.BotProperties;
import backend.academy.linktracker.scrapper.properties.HttpClientProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import java.net.http.HttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({BotProperties.class, HttpClientProperties.class})
public class HttpClientConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    public JdkClientHttpRequestFactory httpRequestFactory(HttpClientProperties props) {
        var httpClient = HttpClient.newBuilder()
                .connectTimeout(props.getConnectTimeout())
                .build();
        var factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(props.getReadTimeout());
        return factory;
    }

    @Bean
    public RestClient botRestClient(BotProperties botProperties, JdkClientHttpRequestFactory factory) {
        return RestClient.builder()
                .baseUrl(botProperties.getBaseUrl())
                .requestFactory(factory)
                .build();
    }

    @Bean
    public RestClient githubRestClient(JdkClientHttpRequestFactory factory) {
        return RestClient.builder()
                .baseUrl("https://api.github.com")
                .requestFactory(factory)
                .build();
    }

    @Bean
    public RestClient stackoverflowRestClient(JdkClientHttpRequestFactory factory) {
        return RestClient.builder()
                .baseUrl("https://api.stackexchange.com/2.3")
                .requestFactory(factory)
                .build();
    }

    @Bean
    public ManagedChannel botGrpcChannel(BotProperties botProperties) {
        return NettyChannelBuilder.forAddress(botProperties.getGrpcHost(), botProperties.getGrpcPort())
                .usePlaintext()
                .build();
    }
}
