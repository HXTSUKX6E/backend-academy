package backend.academy.linktracker.ai.integration;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.ai.service.SummarizationService;
import com.example.notification.LinkUpdateEvent;
import com.example.notification.ProcessedUpdateEvent;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Tag("integration")
@SpringBootTest
@Testcontainers
@Import(KafkaIntegrationTest.StubSummarizationConfig.class)
@DirtiesContext
@TestPropertySource(
        properties = {
            "spring.kafka.producer.properties.schema.registry.url=mock://ai-agent-test",
            "spring.kafka.consumer.properties.schema.registry.url=mock://ai-agent-test",
            "ai-agent.kafka.raw-topic-replicas=1",
            "ai-agent.kafka.processed-topic-replicas=1",
            "ai-agent.grouping.window-ms=100"
        })
class KafkaIntegrationTest {

    @TestConfiguration
    static class StubSummarizationConfig {
        @Bean
        @Primary
        SummarizationService stubSummarizationService() {
            return text -> text.substring(0, Math.min(text.length(), 100)) + "...";
        }
    }

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native:4.1.1"));

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Value("${ai-agent.kafka.raw-topic}")
    private String rawTopic;

    @Value("${ai-agent.kafka.processed-topic}")
    private String processedTopic;

    private KafkaMessageListenerContainer<String, ProcessedUpdateEvent> listenerContainer;
    private BlockingQueue<ProcessedUpdateEvent> receivedEvents;

    @BeforeEach
    void setUp() {
        receivedEvents = new ArrayBlockingQueue<>(10);

        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + UUID.randomUUID());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerProps.put("schema.registry.url", "mock://ai-agent-test");
        consumerProps.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        var factory =
                new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new KafkaAvroDeserializer());
        var containerProps = new ContainerProperties(processedTopic);
        containerProps.setMessageListener(
                (MessageListener<String, ProcessedUpdateEvent>) record -> receivedEvents.offer(record.value()));

        listenerContainer = new KafkaMessageListenerContainer<>(factory, containerProps);
        listenerContainer.start();
        Awaitility.await()
                .atMost(java.time.Duration.ofSeconds(10))
                .until(() -> listenerContainer.getAssignedPartitions() != null
                        && !listenerContainer.getAssignedPartitions().isEmpty());
    }

    @AfterEach
    void tearDown() {
        listenerContainer.stop();
    }

    @Test
    void tc11_shouldReceiveRawUpdateAndPublishProcessedUpdate() throws Exception {
        var rawEvent = LinkUpdateEvent.newBuilder()
                .setId(42L)
                .setUrl("https://github.com/test/repo")
                .setDescription(
                        "This is a valid update with enough length to pass all filters and get processed correctly")
                .setTgChatIds(List.of(100L, 200L))
                .setAuthor("real-developer")
                .build();

        kafkaTemplate.send(rawTopic, String.valueOf(rawEvent.getId()), rawEvent);

        var processed = receivedEvents.poll(20, TimeUnit.SECONDS);
        assertThat(processed).isNotNull();
        assertThat(processed.getId()).isEqualTo(42L);
        assertThat(processed.getUrl()).isEqualTo("https://github.com/test/repo");
        assertThat(processed.getDescription()).isNotBlank();
        assertThat(processed.getPriority()).isEqualTo("MEDIUM");
    }

    @Test
    void tc31_shouldPublishWithHighPriorityWhenDescriptionContainsHighKeyword() throws Exception {
        var rawEvent = LinkUpdateEvent.newBuilder()
                .setId(43L)
                .setUrl("https://github.com/test/repo")
                .setDescription("critical bug fix in authentication module causing security issue")
                .setTgChatIds(List.of(300L))
                .setAuthor("real-developer")
                .build();

        kafkaTemplate.send(rawTopic, String.valueOf(rawEvent.getId()), rawEvent);

        var processed = receivedEvents.poll(20, TimeUnit.SECONDS);
        assertThat(processed).isNotNull();
        assertThat(processed.getId()).isEqualTo(43L);
        assertThat(processed.getPriority()).isEqualTo("HIGH");
    }

    @Test
    void tc31_shouldPublishWithLowPriorityWhenDescriptionContainsLowKeyword() throws Exception {
        var rawEvent = LinkUpdateEvent.newBuilder()
                .setId(44L)
                .setUrl("https://github.com/test/repo")
                .setDescription("fix typo in readme documentation file with enough length here")
                .setTgChatIds(List.of(400L))
                .setAuthor("real-developer")
                .build();

        kafkaTemplate.send(rawTopic, String.valueOf(rawEvent.getId()), rawEvent);

        var processed = receivedEvents.poll(20, TimeUnit.SECONDS);
        assertThat(processed).isNotNull();
        assertThat(processed.getId()).isEqualTo(44L);
        assertThat(processed.getPriority()).isEqualTo("LOW");
    }

    @Test
    void tc32_shouldNotPublishFilteredUpdateWithStopWord() throws Exception {
        var rawEvent = LinkUpdateEvent.newBuilder()
                .setId(99L)
                .setUrl("https://github.com/test/repo")
                .setDescription("This update contains spam content and should be filtered out completely")
                .setTgChatIds(List.of(500L))
                .setAuthor("normal-user")
                .build();

        kafkaTemplate.send(rawTopic, String.valueOf(rawEvent.getId()), rawEvent);

        var processed = receivedEvents.poll(5, TimeUnit.SECONDS);
        assertThat(processed).isNull();
    }

    @Test
    void tc12_shouldNotCrashOnInvalidMessage() throws Exception {
        kafkaTemplate.send(rawTopic, "bad-key", "this is not an avro message");

        var processed = receivedEvents.poll(5, TimeUnit.SECONDS);
        assertThat(processed).isNull();
    }
}
