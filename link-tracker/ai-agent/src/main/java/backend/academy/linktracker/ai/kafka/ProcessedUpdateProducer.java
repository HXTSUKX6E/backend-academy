package backend.academy.linktracker.ai.kafka;

import backend.academy.linktracker.ai.configuration.AiAgentProperties;
import com.example.notification.ProcessedUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessedUpdateProducer {

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final AiAgentProperties properties;

    public void publish(ProcessedUpdateEvent event) {
        String topic = properties.kafka().processedTopic();
        kafkaTemplate.send(topic, String.valueOf(event.getId()), event);
        log.atInfo()
                .addKeyValue("id", event.getId())
                .addKeyValue("topic", topic)
                .log("Processed update published to Kafka");
    }
}
