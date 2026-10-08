package backend.academy.linktracker.bot.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class BotMetrics {

    private static final double[] PERCENTILES = {0.5, 0.95, 0.99};
    private static final double[] SLO_MS = {5, 10, 25, 50, 100, 250, 500, 1000, 2500, 5000};

    private final MeterRegistry registry;
    private final Counter telegramCommandRequests;
    private final Counter telegramMessageRequests;
    private final Counter sentNotifications;
    private final Map<String, Counter> commandCounters = new ConcurrentHashMap<>();
    private final Map<String, DistributionSummary> commandDurations = new ConcurrentHashMap<>();

    public BotMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.telegramCommandRequests = Counter.builder("telegram_requests")
                .description("Total Telegram update requests received")
                .tag("request_type", "command")
                .register(registry);
        this.telegramMessageRequests = Counter.builder("telegram_requests")
                .description("Total Telegram update requests received")
                .tag("request_type", "message")
                .register(registry);
        this.sentNotifications = Counter.builder("sent_notification")
                .description("Total notifications sent to Telegram users")
                .register(registry);
    }

    public void incrementTelegramRequests(boolean isCommand) {
        if (isCommand) {
            telegramCommandRequests.increment();
        } else {
            telegramMessageRequests.increment();
        }
    }

    public void incrementSentNotifications() {
        sentNotifications.increment();
    }

    public void incrementCommandRequests(String command) {
        commandCounters
                .computeIfAbsent(command, cmd -> Counter.builder("command_requests")
                        .description("Total bot commands processed")
                        .tag("command", cmd)
                        .register(registry))
                .increment();
    }

    public void recordCommandDuration(String scope, String scopeType, long durationMs) {
        String key = scope + ":" + scopeType;
        commandDurations
                .computeIfAbsent(key, k -> DistributionSummary.builder("command_duration_ms")
                        .description("Duration of one scrapper API call in milliseconds")
                        .baseUnit("milliseconds")
                        .tag("scope", scope)
                        .tag("scope_type", scopeType)
                        .publishPercentiles(PERCENTILES)
                        .publishPercentileHistogram()
                        .serviceLevelObjectives(SLO_MS)
                        .register(registry))
                .record(durationMs);
    }
}
