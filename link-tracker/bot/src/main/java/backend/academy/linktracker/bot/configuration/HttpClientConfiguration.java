package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.properties.HttpClientProperties;
import backend.academy.linktracker.bot.properties.ScrapperProperties;
import java.net.http.HttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({ScrapperProperties.class, HttpClientProperties.class})
public class HttpClientConfiguration {

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
    public RestClient scrapperRestClient(ScrapperProperties properties, JdkClientHttpRequestFactory factory) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(factory)
                .build();
    }
}
