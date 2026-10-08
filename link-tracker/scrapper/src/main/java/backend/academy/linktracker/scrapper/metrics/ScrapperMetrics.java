package backend.academy.linktracker.scrapper.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ScrapperMetrics {

    private static final double[] PERCENTILES = {0.5, 0.95, 0.99};
    private static final double[] SLO_MS = {5, 10, 25, 50, 100, 250, 500, 1000, 2500, 5000};

    private final DistributionSummary githubDuration;
    private final DistributionSummary stackoverflowDuration;
    private final DistributionSummary dbDuration;
    private final Counter githubApiRequests;
    private final Counter stackoverflowApiRequests;

    public ScrapperMetrics(MeterRegistry registry) {
        this.githubDuration = buildDurationSummary(registry, "external_source", "github.com");
        this.stackoverflowDuration = buildDurationSummary(registry, "external_source", "stackoverflow.com");
        this.dbDuration = buildDurationSummary(registry, "database", "links");
        this.githubApiRequests = Counter.builder("api_requests")
                .description("Total API requests to external sources")
                .tag("source", "github")
                .register(registry);
        this.stackoverflowApiRequests = Counter.builder("api_requests")
                .description("Total API requests to external sources")
                .tag("source", "stackoverflow")
                .register(registry);
    }

    public void recordGithubDuration(long durationMs) {
        githubDuration.record(durationMs);
        githubApiRequests.increment();
    }

    public void recordStackoverflowDuration(long durationMs) {
        stackoverflowDuration.record(durationMs);
        stackoverflowApiRequests.increment();
    }

    public void recordDbDuration(long durationMs) {
        dbDuration.record(durationMs);
    }

    private static DistributionSummary buildDurationSummary(MeterRegistry registry, String scope, String scopeType) {
        return DistributionSummary.builder("request_duration_ms")
                .description("Duration of one operation in milliseconds")
                .baseUnit("milliseconds")
                .tag("scope", scope)
                .tag("scope_type", scopeType)
                .publishPercentiles(PERCENTILES)
                .publishPercentileHistogram()
                .serviceLevelObjectives(SLO_MS)
                .register(registry);
    }
}
