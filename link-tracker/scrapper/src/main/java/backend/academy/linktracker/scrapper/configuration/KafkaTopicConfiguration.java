package backend.academy.linktracker.scrapper.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Conditional(KafkaModeCondition.class)
public class KafkaTopicConfiguration {

    @Value("${app.kafka.topic}")
    private String topic;

    @Value("${app.kafka.topic-partitions:3}")
    private int partitions;

    @Value("${app.kafka.topic-replicas:3}")
    private int replicas;

    @Bean
    public NewTopic linkUpdatesTopic() {
        return TopicBuilder.name(topic)
                .partitions(partitions)
                .replicas(replicas)
                .config("min.insync.replicas", "2")
                .build();
    }
}
