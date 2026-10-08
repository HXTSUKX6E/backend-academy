package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.GithubIssueDto;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.LinkUpdate.UpdateType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class GithubClient {

    private static final String ISSUES_URI =
            "/repos/{owner}/{repo}/issues?state=all&sort=created&direction=desc&per_page=100";
    private static final ParameterizedTypeReference<List<GithubIssueDto>> LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient githubRestClient;

    @Retry(name = "external")
    @CircuitBreaker(name = "github")
    public List<LinkUpdate> fetchLatestUpdates(String owner, String repo, Instant since) {
        List<GithubIssueDto> items =
                githubRestClient.get().uri(ISSUES_URI, owner, repo).retrieve().body(LIST_TYPE);

        if (items == null || items.isEmpty()) {
            return List.of();
        }

        return items.stream()
                .filter(item -> item.createdAt() != null
                        && Instant.parse(item.createdAt()).isAfter(since))
                .map(this::toUpdate)
                .toList();
    }

    private LinkUpdate toUpdate(GithubIssueDto item) {
        Instant createdAt = Instant.parse(item.createdAt());
        String author =
                item.user() != null && item.user().login() != null ? item.user().login() : "";
        return LinkUpdate.of(
                item.title() != null ? item.title() : "",
                author,
                createdAt,
                item.body(),
                createdAt,
                UpdateType.GITHUB_ISSUE);
    }
}
