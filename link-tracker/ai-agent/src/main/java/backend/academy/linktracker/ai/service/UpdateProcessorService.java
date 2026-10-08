package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.configuration.AiAgentProperties;
import com.example.notification.LinkUpdateEvent;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateProcessorService {

    private final FilterService filterService;
    private final SummarizationService summarizationService;
    private final PrioritizationService prioritizationService;
    private final GroupingService groupingService;
    private final AiAgentProperties properties;

    public void process(LinkUpdateEvent event) {
        String author = event.getAuthor();
        String description = event.getDescription();

        if (!filterService.shouldProcess(description, author)) {
            log.atInfo()
                    .addKeyValue("id", event.getId())
                    .addKeyValue("author", author)
                    .log("Update filtered out, skipping");
            return;
        }

        String processedDescription = maybeSummarize(description);
        Priority priority = prioritizationService.prioritize(processedDescription);

        var update = new PrioritizedUpdate(
                event.getId(), event.getUrl(), processedDescription, new ArrayList<>(event.getTgChatIds()), priority);

        groupingService.buffer(update);

        log.atInfo()
                .addKeyValue("id", event.getId())
                .addKeyValue("priority", priority)
                .addKeyValue("summarized", !processedDescription.equals(description))
                .log("Update prioritized and buffered");
    }

    private String maybeSummarize(String description) {
        int threshold = properties.summarization().threshold();
        if (description.length() > threshold) {
            return summarizationService.summarize(description);
        }
        return description;
    }
}
