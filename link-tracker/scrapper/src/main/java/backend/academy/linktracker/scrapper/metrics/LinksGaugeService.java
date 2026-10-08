package backend.academy.linktracker.scrapper.metrics;

import backend.academy.linktracker.scrapper.repository.LinkRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class LinksGaugeService {

    public LinksGaugeService(MeterRegistry registry, LinkRepository linkRepository) {
        Gauge.builder("links_on_track_total", linkRepository, r -> r.getAllTrackedLinks().stream()
                        .filter(l -> l.getUrl().contains("github.com"))
                        .count())
                .description("Number of links on monitoring by domain")
                .tag("tracked_source", "github")
                .register(registry);

        Gauge.builder("links_on_track_total", linkRepository, r -> r.getAllTrackedLinks().stream()
                        .filter(l -> l.getUrl().contains("stackoverflow.com"))
                        .count())
                .description("Number of links on monitoring by domain")
                .tag("tracked_source", "stackoverflow")
                .register(registry);
    }
}
