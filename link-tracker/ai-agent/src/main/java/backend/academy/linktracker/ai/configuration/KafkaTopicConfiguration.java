package backend.academy.linktracker.ai.configuration;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfiguration {

    private final AiAgentProperties properties;

    @Bean
    public NewTopic rawUpdatesTopic() {
        var kafka = properties.kafka();
        return TopicBuilder.name(kafka.rawTopic())
                .partitions(kafka.rawTopicPartitions())
                .replicas(kafka.rawTopicReplicas())
                .build();
    }

    @Bean
    public NewTopic processedUpdatesTopic() {
        var kafka = properties.kafka();
        return TopicBuilder.name(kafka.processedTopic())
                .partitions(kafka.processedTopicPartitions())
                .replicas(kafka.processedTopicReplicas())
                .build();
    }
}
