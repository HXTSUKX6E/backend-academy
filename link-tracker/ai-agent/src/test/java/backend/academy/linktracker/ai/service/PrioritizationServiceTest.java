package backend.academy.linktracker.ai.service;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.ai.configuration.AiAgentProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PrioritizationServiceTest {

    private PrioritizationService prioritizationService;

    @BeforeEach
    void setUp() {
        prioritizationService = new PrioritizationService(buildProps());
    }

    @Test
    void tc11_shouldReturnHighPriorityWhenContainsCritical() {
        assertThat(prioritizationService.prioritize("critical bug fix in release"))
                .isEqualTo(Priority.HIGH);
    }

    @Test
    void tc11_shouldReturnHighPriorityWhenContainsSecurityKeyword() {
        assertThat(prioritizationService.prioritize("security patch for CVE-2024-0001"))
                .isEqualTo(Priority.HIGH);
    }

    @Test
    void tc11_shouldReturnHighPriorityWhenContainsBreaking() {
        assertThat(prioritizationService.prioritize("breaking change in public API"))
                .isEqualTo(Priority.HIGH);
    }

    @Test
    void tc12_shouldReturnMediumPriorityWhenNoMatchingKeywords() {
        assertThat(prioritizationService.prioritize("added new feature for user profiles"))
                .isEqualTo(Priority.MEDIUM);
    }

    @Test
    void tc13_shouldReturnLowPriorityWhenContainsTypo() {
        assertThat(prioritizationService.prioritize("fix typo in readme file")).isEqualTo(Priority.LOW);
    }

    @Test
    void tc13_shouldReturnLowPriorityWhenContainsDocs() {
        assertThat(prioritizationService.prioritize("updated docs for new endpoint"))
                .isEqualTo(Priority.LOW);
    }

    @Test
    void shouldPreferHighOverLowWhenBothKeywordsPresent() {
        assertThat(prioritizationService.prioritize("critical typo in security docs"))
                .isEqualTo(Priority.HIGH);
    }

    @Test
    void shouldMatchKeywordsCaseInsensitively() {
        assertThat(prioritizationService.prioritize("CRITICAL system failure")).isEqualTo(Priority.HIGH);
    }

    private AiAgentProperties buildProps() {
        var kafkaProps = new AiAgentProperties.KafkaProperties(
                "link.raw-updates",
                "link.processed-updates",
                3,
                1,
                3,
                1,
                new AiAgentProperties.KafkaProperties.ConsumerProperties(3, 1000));
        var filtering = new AiAgentProperties.FilteringProperties(List.of(), List.of(), 20);
        var hf = new AiAgentProperties.SummarizationProperties.HuggingFaceProperties(
                "", "https://api-inference.huggingface.co", "facebook/bart-large-cnn");
        var summarization = new AiAgentProperties.SummarizationProperties(500, hf);
        var prioritization = new AiAgentProperties.PrioritizationProperties(
                List.of("critical", "urgent", "breaking", "security"), List.of("minor", "typo", "chore", "docs"));
        var grouping = new AiAgentProperties.GroupingProperties(30000L);
        return new AiAgentProperties(kafkaProps, filtering, summarization, prioritization, grouping);
    }
}
