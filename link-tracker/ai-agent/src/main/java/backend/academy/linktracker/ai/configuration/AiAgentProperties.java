package backend.academy.linktracker.ai.configuration;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai-agent")
public record AiAgentProperties(
        KafkaProperties kafka,
        FilteringProperties filtering,
        SummarizationProperties summarization,
        PrioritizationProperties prioritization,
        GroupingProperties grouping) {

    public record KafkaProperties(
            String rawTopic,
            String processedTopic,
            int rawTopicPartitions,
            int rawTopicReplicas,
            int processedTopicPartitions,
            int processedTopicReplicas,
            ConsumerProperties consumer) {
        public record ConsumerProperties(int maxAttempts, long backoffInterval) {}
    }

    public record FilteringProperties(List<String> stopWords, List<String> excludedAuthors, int minLength) {}

    public record SummarizationProperties(int threshold, HuggingFaceProperties huggingFace) {
        public record HuggingFaceProperties(String apiKey, String baseUrl, String model) {}
    }

    public record PrioritizationProperties(List<String> highKeywords, List<String> lowKeywords) {}

    public record GroupingProperties(long windowMs) {}
}
