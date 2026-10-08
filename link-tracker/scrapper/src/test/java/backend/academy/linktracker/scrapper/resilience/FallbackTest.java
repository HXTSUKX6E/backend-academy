package backend.academy.linktracker.scrapper.resilience;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.TestcontainersConfiguration;
import backend.academy.linktracker.scrapper.client.BotNotifierClient;
import backend.academy.linktracker.scrapper.dto.LinkUpdateRequest;
import com.example.notification.LinkUpdateEvent;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.kafka.KafkaContainer;
import org.wiremock.spring.EnableWireMock;

@Tag("integration")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@DirtiesContext
@EnableWireMock
@TestPropertySource(
        properties = {
            "app.bot.protocol=rest",
            "spring.kafka.producer.properties.schema.registry.url=mock://fallback-test",
            "app.bot.base-url=http://localhost:${wiremock.server.port}",
            "app.circuit-breaker.sliding-window-size=1",
            "app.circuit-breaker.failure-rate-threshold=100",
            "app.circuit-breaker.wait-duration-in-open-state=1s",
            "app.retry.max-attempts=1"
        })
class FallbackTest {

    @Autowired
    private BotNotifierClient botNotifierClient;

    @Autowired
    private KafkaContainer kafka;

    @Value("${app.kafka.topic}")
    private String topic;

    private KafkaMessageListenerContainer<String, LinkUpdateEvent> listenerContainer;
    private BlockingQueue<LinkUpdateEvent> received;

    @BeforeEach
    void setUp() {
        stubFor(post(urlPathEqualTo("/updates")).willReturn(aResponse().withStatus(503)));

        received = new ArrayBlockingQueue<>(10);

        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "fallback-test-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerProps.put("schema.registry.url", "mock://fallback-test");
        consumerProps.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        var factory =
                new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new KafkaAvroDeserializer());
        var containerProps = new ContainerProperties(topic);
        containerProps.setMessageListener(
                (MessageListener<String, LinkUpdateEvent>) record -> received.offer(record.value()));

        listenerContainer = new KafkaMessageListenerContainer<>(factory, containerProps);
        listenerContainer.start();
    }

    @AfterEach
    void tearDown() {
        listenerContainer.stop();
    }

    @Test
    void tc51_whenHttpUnavailable_fallsBackToKafka() throws Exception {
        var request = new LinkUpdateRequest(77L, "https://github.com/test/repo", "Update description", List.of(1L));

        botNotifierClient.sendUpdate(request);

        LinkUpdateEvent event = received.poll(15, TimeUnit.SECONDS);
        assertThat(event).isNotNull();
        assertThat(event.getId()).isEqualTo(77L);
        assertThat(event.getUrl()).isEqualTo("https://github.com/test/repo");
    }
}
