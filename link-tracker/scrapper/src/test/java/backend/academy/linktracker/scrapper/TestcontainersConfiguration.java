package backend.academy.linktracker.scrapper;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer("postgres:16-alpine")
                .withDatabaseName("scrapper")
                .withUsername("postgres")
                .withPassword("postgres")
                .withInitScript("test-init.sql");
    }

    @Bean
    @ServiceConnection("redis")
    @SuppressWarnings("resource")
    GenericContainer<?> valkeyContainer() {
        return new GenericContainer<>("valkey/valkey:8").withExposedPorts(6379);
    }

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("apache/kafka-native:4.1.1"));
    }
}
