package backend.academy.linktracker.ai.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.ai.configuration.AiAgentProperties;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class SummarizationServiceTest {

    private WireMockServer wireMockServer;
    private HuggingFaceSummarizationService summarizationService;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();

        summarizationService = new HuggingFaceSummarizationService(
                RestClient.builder(), buildProps("test-api-key", wireMockServer.port(), 500));
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void tc31_shouldSummarizeLongText() {
        wireMockServer.stubFor(post(urlPathMatching("/models/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"summary_text\": \"Short summary of the update.\"}]")));

        String longText = "a".repeat(600);
        String result = summarizationService.summarize(longText);

        assertThat(result).isEqualTo("Short summary of the update.");
        assertThat(result.length()).isLessThan(longText.length());
    }

    @Test
    void tc31_shouldFallbackToTruncationWhenApiFails() {
        wireMockServer.stubFor(
                post(urlPathMatching("/models/.*")).willReturn(aResponse().withStatus(500)));

        String longText = "a".repeat(600);
        String result = summarizationService.summarize(longText);

        assertThat(result).endsWith("...");
        assertThat(result.length()).isEqualTo(503);
    }

    @Test
    void tc32_shouldReturnTextUnchangedForShortInput() {
        var service =
                new HuggingFaceSummarizationService(RestClient.builder(), buildProps("", wireMockServer.port(), 500));
        String shortText = "a".repeat(200);

        String result = service.summarize(shortText);

        assertThat(result).isEqualTo(shortText);
    }

    @Test
    void shouldFallbackToTruncationWhenNoApiKey() {
        var service =
                new HuggingFaceSummarizationService(RestClient.builder(), buildProps("", wireMockServer.port(), 100));
        String longText = "a".repeat(200);

        String result = service.summarize(longText);

        assertThat(result).endsWith("...");
        assertThat(result.length()).isEqualTo(103);
    }

    private AiAgentProperties buildProps(String apiKey, int port, int threshold) {
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
                apiKey, "http://localhost:" + port, "facebook/bart-large-cnn");
        var summarization = new AiAgentProperties.SummarizationProperties(threshold, hf);
        var prioritization = new AiAgentProperties.PrioritizationProperties(
                List.of("critical", "urgent", "breaking", "security"), List.of("minor", "typo", "chore", "docs"));
        var grouping = new AiAgentProperties.GroupingProperties(30000L);
        return new AiAgentProperties(kafkaProps, filtering, summarization, prioritization, grouping);
    }
}
