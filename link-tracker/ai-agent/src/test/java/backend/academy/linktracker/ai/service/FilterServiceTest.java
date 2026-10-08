package backend.academy.linktracker.ai.service;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.ai.configuration.AiAgentProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FilterServiceTest {

    private FilterService filterService;

    @BeforeEach
    void setUp() {
        var filtering = new AiAgentProperties.FilteringProperties(
                List.of("spam", "ads", "promo"), List.of("bot-user", "evil-bot"), 20);
        var kafkaProps = new AiAgentProperties.KafkaProperties(
                "link.raw-updates",
                "link.processed-updates",
                3,
                1,
                3,
                1,
                new AiAgentProperties.KafkaProperties.ConsumerProperties(3, 1000));
        var hf = new AiAgentProperties.SummarizationProperties.HuggingFaceProperties(
                "", "https://api-inference.huggingface.co", "facebook/bart-large-cnn");
        var summarization = new AiAgentProperties.SummarizationProperties(500, hf);
        var prioritization = new AiAgentProperties.PrioritizationProperties(
                List.of("critical", "urgent", "breaking", "security"), List.of("minor", "typo", "chore", "docs"));
        var grouping = new AiAgentProperties.GroupingProperties(30000L);
        var props = new AiAgentProperties(kafkaProps, filtering, summarization, prioritization, grouping);
        filterService = new FilterService(props);
    }

    @Test
    void tc21_shouldFilterOutWhenContainsStopWord() {
        assertThat(filterService.shouldProcess(
                        "This is a spam message with enough length to pass minimum", "normal-user"))
                .isFalse();
    }

    @Test
    void tc21_shouldFilterOutWhenContainsAds() {
        assertThat(filterService.shouldProcess("Check out these great ads for products today!", "normal-user"))
                .isFalse();
    }

    @Test
    void tc22_shouldFilterOutWhenExcludedAuthor() {
        assertThat(filterService.shouldProcess(
                        "This is a perfectly valid update with enough length to pass", "bot-user"))
                .isFalse();
    }

    @Test
    void tc22_shouldFilterOutCaseInsensitiveAuthor() {
        assertThat(filterService.shouldProcess(
                        "This is a perfectly valid update with enough length to pass", "BOT-USER"))
                .isFalse();
    }

    @Test
    void tc23_shouldFilterOutWhenTextTooShort() {
        assertThat(filterService.shouldProcess("Short text", "normal-user")).isFalse();
    }

    @Test
    void tc23_shouldFilterOutNullDescription() {
        assertThat(filterService.shouldProcess(null, "normal-user")).isFalse();
    }

    @Test
    void tc24_shouldPassValidUpdate() {
        assertThat(filterService.shouldProcess(
                        "This is a perfectly valid update with enough length to pass all filters", "real-developer"))
                .isTrue();
    }

    @Test
    void tc24_shouldPassUpdateWithNullAuthor() {
        assertThat(filterService.shouldProcess(
                        "This is a perfectly valid update with enough length to pass all filters", null))
                .isTrue();
    }
}
