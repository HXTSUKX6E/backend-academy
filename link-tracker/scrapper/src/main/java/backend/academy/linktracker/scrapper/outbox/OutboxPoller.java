package backend.academy.linktracker.scrapper.outbox;

import backend.academy.linktracker.scrapper.dto.LinkUpdateRequest;
import com.example.notification.LinkUpdateEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.bot", name = "protocol", havingValue = "kafka-outbox")
public class OutboxPoller {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic}")
    private String topic;

    @Value("${app.outbox.batch-size:100}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval:5000}")
    @Transactional
    public void poll() {
        var events = outboxRepository.findUnpublished(batchSize);
        for (var event : events) {
            try {
                publish(event);
                outboxRepository.markPublished(event.getId());
            } catch (Exception e) {
                log.atError()
                        .addKeyValue("eventId", event.getId())
                        .setCause(e)
                        .log("Failed to publish outbox event — will retry on next poll");
            }
        }
    }

    private void publish(OutboxEvent event) throws JsonProcessingException {
        var request = objectMapper.readValue(event.getPayload(), LinkUpdateRequest.class);
        var avroEvent = LinkUpdateEvent.newBuilder()
                .setId(request.id())
                .setUrl(request.url())
                .setDescription(request.description())
                .setTgChatIds(request.tgChatIds())
                .setAuthor(request.author())
                .build();
        kafkaTemplate.send(topic, String.valueOf(event.getAggregateId()), avroEvent);
        log.atInfo()
                .addKeyValue("url", request.url())
                .addKeyValue("eventId", event.getId())
                .log("Outbox event published to Kafka");
    }
}
