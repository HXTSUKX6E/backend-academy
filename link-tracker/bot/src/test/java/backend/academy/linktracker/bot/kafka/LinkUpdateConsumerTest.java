package backend.academy.linktracker.bot.kafka;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import com.example.notification.ProcessedUpdateEvent;
import com.pengrad.telegrambot.TelegramBot;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Tag("integration")
@SpringBootTest
@Testcontainers
@DirtiesContext
@TestPropertySource(
        properties = {
            "app.telegram.token=test-token",
            "app.telegram.polling.enabled=false",
            "app.telegram.url=http://localhost:9999/bot",
            "spring.kafka.properties.schema.registry.url=mock://bot-test",
            "spring.kafka.consumer.properties.schema.registry.url=mock://bot-test",
            "spring.kafka.producer.properties.schema.registry.url=mock://bot-test"
        })
class LinkUpdateConsumerTest {

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native:4.1.1"));

    @MockitoBean
    private TelegramBot telegramBot;

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topic;

    @Test
    void shouldConsumeKafkaMessageAndForwardToTelegram() {
        var event = ProcessedUpdateEvent.newBuilder()
                .setId(1L)
                .setUrl("https://github.com/test/repo")
                .setDescription("New issue opened")
                .setTgChatIds(List.of(100L, 200L))
                .setPriority("NORMAL")
                .build();

        kafkaTemplate.send(topic, String.valueOf(event.getId()), event);

        await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> verify(telegramBot, atLeastOnce()).execute(any()));
    }
}
