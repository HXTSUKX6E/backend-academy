package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.LinkUpdateRequest;
import com.example.notification.LinkUpdateEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.bot", name = "protocol", havingValue = "rest")
public class RestBotClient implements BotNotifierClient {

    private final RestClient botRestClient;
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topic;

    @Retry(name = "botRest")
    @CircuitBreaker(name = "botRest", fallbackMethod = "fallbackToKafka")
    @Override
    public void sendUpdate(LinkUpdateRequest request) {
        botRestClient.post().uri("/updates").body(request).retrieve().toBodilessEntity();
        log.atInfo()
                .addKeyValue("url", request.url())
                .addKeyValue("chatCount", request.tgChatIds().size())
                .log("Update sent to bot via REST");
    }

    @SuppressWarnings("unused")
    private void fallbackToKafka(LinkUpdateRequest request, Throwable cause) {
        log.atWarn()
                .addKeyValue("url", request.url())
                .setCause(cause)
                .log("REST bot unavailable, falling back to Kafka");
        var event = LinkUpdateEvent.newBuilder()
                .setId(request.id())
                .setUrl(request.url())
                .setDescription(request.description())
                .setTgChatIds(request.tgChatIds())
                .setAuthor(request.author())
                .build();
        kafkaTemplate.send(topic, String.valueOf(request.id()), event);
    }
}
