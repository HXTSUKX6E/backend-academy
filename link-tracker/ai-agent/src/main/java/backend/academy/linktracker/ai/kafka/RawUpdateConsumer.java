package backend.academy.linktracker.ai.kafka;

import backend.academy.linktracker.ai.service.UpdateProcessorService;
import com.example.notification.LinkUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RawUpdateConsumer {

    private final UpdateProcessorService processorService;

    @KafkaListener(topics = "${ai-agent.kafka.raw-topic}", containerFactory = "kafkaListenerContainerFactory")
    public void consume(LinkUpdateEvent event) {
        log.atInfo()
                .addKeyValue("id", event.getId())
                .addKeyValue("author", event.getAuthor())
                .log("Received raw update from Kafka");
        processorService.process(event);
    }
}
