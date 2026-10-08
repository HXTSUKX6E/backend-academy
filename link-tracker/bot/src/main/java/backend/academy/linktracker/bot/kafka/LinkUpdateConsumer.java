package backend.academy.linktracker.bot.kafka;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.grpc.UpdateNotifier;
import com.example.notification.ProcessedUpdateEvent;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkUpdateConsumer {

    private final UpdateNotifier updateNotifier;

    @KafkaListener(topics = "${app.kafka.topic}", containerFactory = "kafkaListenerContainerFactory")
    public void consume(ProcessedUpdateEvent event) {
        log.atInfo()
                .addKeyValue("url", event.getUrl())
                .addKeyValue("chatCount", event.getTgChatIds().size())
                .log("Received processed Kafka update (Avro), forwarding to Telegram");
        var update = new LinkUpdate(
                event.getId(), event.getUrl(), event.getDescription(), new ArrayList<>(event.getTgChatIds()), null);
        updateNotifier.notifyUpdate(update);
    }
}
