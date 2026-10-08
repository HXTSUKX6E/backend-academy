package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.outbox.OutboxEvent;
import backend.academy.linktracker.scrapper.outbox.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.bot", name = "protocol", havingValue = "kafka-outbox")
public class OutboxBotClient implements BotNotifierClient {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void sendUpdate(LinkUpdateRequest request) {
        try {
            String payload = objectMapper.writeValueAsString(request);
            outboxRepository.save(OutboxEvent.builder()
                    .aggregateId(request.id())
                    .eventType("LINK_UPDATE")
                    .payload(payload)
                    .build());
            log.atDebug()
                    .addKeyValue("url", request.url())
                    .addKeyValue("chatCount", request.tgChatIds().size())
                    .log("Update saved to outbox");
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox payload", e);
        }
    }
}
