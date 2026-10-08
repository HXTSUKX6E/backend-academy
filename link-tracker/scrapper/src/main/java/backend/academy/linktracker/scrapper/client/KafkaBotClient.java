package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.LinkUpdateRequest;
import com.example.notification.LinkUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.bot", name = "protocol", havingValue = "kafka", matchIfMissing = true)
public class KafkaBotClient implements BotNotifierClient {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topic;

    @Override
    public void sendUpdate(LinkUpdateRequest request) {
        var event = LinkUpdateEvent.newBuilder()
                .setId(request.id())
                .setUrl(request.url())
                .setDescription(request.description())
                .setTgChatIds(request.tgChatIds())
                .setAuthor(request.author())
                .build();
        kafkaTemplate.send(topic, String.valueOf(request.id()), event);
        log.atInfo()
                .addKeyValue("url", request.url())
                .addKeyValue("chatCount", request.tgChatIds().size())
                .log("Update sent to bot via Kafka (Avro)");
    }
}
