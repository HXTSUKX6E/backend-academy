package backend.academy.linktracker.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@EnableWireMock
class StackoverflowClientTest {

    @InjectWireMock
    private WireMockServer wireMock;

    private StackoverflowClient client;

    @BeforeEach
    void setUp() {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + wireMock.port())
                .build();
        client = new StackoverflowClient(restClient);
    }

    @Test
    void returnsUpdateWhenNewAnswerExistsAfterSince() {
        stubAnswers("""
                {
                  "items": [
                    {
                      "creation_date": 1800000000,
                      "owner": {"display_name": "carol"},
                      "body_markdown": "Here is my answer."
                    }
                  ]
                }
                """);
        stubQuestionTitle("How to do X?");

        Instant since = Instant.ofEpochSecond(1_000_000);
        List<LinkUpdate> results = client.fetchLatestAnswers(42L, since);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().title()).isEqualTo("How to do X?");
        assertThat(results.getFirst().author()).isEqualTo("carol");
        assertThat(results.getFirst().bodyPreview()).isEqualTo("Here is my answer.");
    }

    @Test
    void returnsEmptyWhenAnswerIsOlderThanSince() {
        stubAnswers("""
                {
                  "items": [
                    {
                      "creation_date": 500000,
                      "owner": {"display_name": "dave"},
                      "body_markdown": "Old answer."
                    }
                  ]
                }
                """);

        Instant since = Instant.ofEpochSecond(1_000_000);
        List<LinkUpdate> results = client.fetchLatestAnswers(42L, since);

        assertThat(results).isEmpty();
    }

    @Test
    void returnsEmptyWhenNoAnswers() {
        stubAnswers("""
                {"items": []}
                """);

        List<LinkUpdate> results = client.fetchLatestAnswers(42L, Instant.EPOCH);

        assertThat(results).isEmpty();
    }

    @Test
    void throwsOn500() {
        stubFor(get(urlPathEqualTo("/questions/42/answers"))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> client.fetchLatestAnswers(42L, Instant.EPOCH))
                .isInstanceOf(org.springframework.web.client.HttpServerErrorException.class);
    }

    @Test
    void bodyPreviewIsTruncatedTo200Chars() {
        String longAnswer = "y".repeat(400);
        stubAnswers("""
                {
                  "items": [
                    {
                      "creation_date": 9999999999,
                      "owner": {"display_name": "eve"},
                      "body_markdown": "%s"
                    }
                  ]
                }
                """.formatted(longAnswer));
        stubQuestionTitle("Long answer question");

        List<LinkUpdate> results = client.fetchLatestAnswers(42L, Instant.EPOCH);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().bodyPreview()).hasSize(200);
    }

    private void stubAnswers(String body) {
        stubFor(get(urlPathEqualTo("/questions/42/answers"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(body)));
    }

    private void stubQuestionTitle(String title) {
        stubFor(get(urlPathEqualTo("/questions/42"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {"items": [{"title": "%s"}]}
                                """.formatted(title))));
    }
}
