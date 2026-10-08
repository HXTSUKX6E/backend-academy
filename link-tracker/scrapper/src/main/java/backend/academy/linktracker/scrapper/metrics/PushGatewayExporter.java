package backend.academy.linktracker.scrapper.metrics;

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.metrics.pushgateway.enabled", havingValue = "true")
public class PushGatewayExporter {

    private final PrometheusMeterRegistry registry;
    private final RestClient restClient;
    private final String jobName;

    public PushGatewayExporter(
            PrometheusMeterRegistry registry,
            @Value("${app.metrics.pushgateway.base-url:http://localhost:9091}") String baseUrl,
            @Value("${spring.application.name}") String jobName) {
        this.registry = registry;
        this.restClient = RestClient.create(baseUrl);
        this.jobName = jobName;
    }

    @Scheduled(fixedDelayString = "${app.metrics.pushgateway.push-rate:15000}")
    public void push() {
        try {
            String metrics = registry.scrape();
            restClient
                    .post()
                    .uri("/metrics/job/{job}", jobName)
                    .contentType(MediaType.parseMediaType("text/plain; version=0.0.4; charset=utf-8"))
                    .body(metrics)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.atWarn().setCause(e).log("Failed to push metrics to Pushgateway");
        }
    }
}
