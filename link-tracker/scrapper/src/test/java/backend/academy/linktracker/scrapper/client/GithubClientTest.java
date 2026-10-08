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
class GithubClientTest {

    @InjectWireMock
    private WireMockServer wireMock;

    private GithubClient client;

    @BeforeEach
    void setUp() {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + wireMock.port())
                .build();
        client = new GithubClient(restClient);
    }

    @Test
    void returnsUpdateWhenNewIssueCreatedAfterSince() {
        stubFor(get(urlPathEqualTo("/repos/owner/repo/issues"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                [
                                  {
                                    "title": "Fix critical bug",
                                    "created_at": "2024-06-01T12:00:00Z",
                                    "user": {"login": "alice"},
                                    "body": "This is the body of the issue."
                                  }
                                ]
                                """)));

        Instant since = Instant.parse("2024-01-01T00:00:00Z");
        List<LinkUpdate> results = client.fetchLatestUpdates("owner", "repo", since);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().title()).isEqualTo("Fix critical bug");
        assertThat(results.getFirst().author()).isEqualTo("alice");
        assertThat(results.getFirst().bodyPreview()).isEqualTo("This is the body of the issue.");
        assertThat(results.getFirst().createdAt()).isEqualTo(Instant.parse("2024-06-01T12:00:00Z"));
    }

    @Test
    void returnsEmptyWhenIssueIsOlderThanSince() {
        stubFor(get(urlPathEqualTo("/repos/owner/repo/issues"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                [
                                  {
                                    "title": "Old issue",
                                    "created_at": "2023-01-01T10:00:00Z",
                                    "user": {"login": "bob"},
                                    "body": "Old body"
                                  }
                                ]
                                """)));

        Instant since = Instant.parse("2024-01-01T00:00:00Z");
        List<LinkUpdate> results = client.fetchLatestUpdates("owner", "repo", since);

        assertThat(results).isEmpty();
    }

    @Test
    void returnsEmptyWhenResponseIsEmptyList() {
        stubFor(get(urlPathEqualTo("/repos/owner/repo/issues"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("[]")));

        List<LinkUpdate> results = client.fetchLatestUpdates("owner", "repo", Instant.EPOCH);

        assertThat(results).isEmpty();
    }

    @Test
    void throwsOn500() {
        stubFor(get(urlPathEqualTo("/repos/owner/repo/issues"))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> client.fetchLatestUpdates("owner", "repo", Instant.EPOCH))
                .isInstanceOf(org.springframework.web.client.HttpServerErrorException.class);
    }

    @Test
    void bodyPreviewIsTruncatedTo200Chars() {
        String longBody = "x".repeat(300);
        stubFor(get(urlPathEqualTo("/repos/owner/repo/issues"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                [
                                  {
                                    "title": "T",
                                    "created_at": "2025-01-01T00:00:00Z",
                                    "user": {"login": "u"},
                                    "body": "%s"
                                  }
                                ]
                                """.formatted(longBody))));

        List<LinkUpdate> results = client.fetchLatestUpdates("owner", "repo", Instant.EPOCH);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().bodyPreview()).hasSize(200);
    }
}
