package backend.academy.linktracker.scrapper.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.TestcontainersConfiguration;
import com.example.notification.LinkUpdateEvent;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.kafka.KafkaContainer;

@Tag("integration")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@DirtiesContext
@TestPropertySource(
        properties = {
            "app.bot.protocol=kafka-outbox",
            "app.outbox.poll-interval=500",
            "spring.kafka.producer.properties.schema.registry.url=mock://outbox-test"
        })
class OutboxIntegrationTest {

    @Autowired
    KafkaContainer kafka;

    @Autowired
    private OutboxPoller outboxPoller;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${app.kafka.topic}")
    private String topic;

    private KafkaMessageListenerContainer<String, LinkUpdateEvent> listenerContainer;
    private BlockingQueue<LinkUpdateEvent> receivedEvents;

    @BeforeEach
    void setUp() {
        receivedEvents = new ArrayBlockingQueue<>(10);

        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-outbox-consumer");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerProps.put("schema.registry.url", "mock://outbox-test");
        consumerProps.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        var factory =
                new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new KafkaAvroDeserializer());
        var containerProps = new ContainerProperties(topic);
        containerProps.setMessageListener(
                (MessageListener<String, LinkUpdateEvent>) record -> receivedEvents.offer(record.value()));

        listenerContainer = new KafkaMessageListenerContainer<>(factory, containerProps);
        listenerContainer.start();
    }

    @AfterEach
    void tearDown() {
        listenerContainer.stop();
        jdbcTemplate.update("DELETE FROM outbox_events");
    }

    @Test
    void shouldPublishOutboxEventToKafkaAndMarkPublished() throws Exception {
        jdbcTemplate.update(
                "INSERT INTO outbox_events (aggregate_id, event_type, payload) " + "VALUES (?, ?, ?::jsonb)",
                42L,
                "LINK_UPDATE",
                "{\"id\":42,\"url\":\"https://github.com/test/repo\","
                        + "\"description\":\"New commit pushed\",\"tgChatIds\":[100,200]}");

        outboxPoller.poll();

        var event = receivedEvents.poll(15, TimeUnit.SECONDS);
        assertThat(event).isNotNull();
        assertThat(event.getId()).isEqualTo(42L);
        assertThat(event.getUrl()).isEqualTo("https://github.com/test/repo");
        assertThat(event.getDescription()).isEqualTo("New commit pushed");
        assertThat(event.getTgChatIds()).containsExactly(100L, 200L);

        var publishedAt = jdbcTemplate.queryForObject(
                "SELECT published_at FROM outbox_events WHERE aggregate_id = 42", java.sql.Timestamp.class);
        assertThat(publishedAt).isNotNull();
    }
}
