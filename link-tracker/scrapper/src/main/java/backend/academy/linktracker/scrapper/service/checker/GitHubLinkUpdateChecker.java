package backend.academy.linktracker.scrapper.service.checker;

import backend.academy.linktracker.scrapper.client.GithubClient;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.metrics.ScrapperMetrics;
import backend.academy.linktracker.scrapper.service.LinkUpdateChecker;
import backend.academy.linktracker.scrapper.util.LinkUrlParser;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GitHubLinkUpdateChecker implements LinkUpdateChecker {

    private final GithubClient githubClient;
    private final ScrapperMetrics metrics;

    @Override
    public boolean supports(String url) {
        return LinkUrlParser.parseGitHubRepo(url).isPresent();
    }

    @Override
    public List<LinkUpdate> check(String url, Instant since) {
        long start = System.currentTimeMillis();
        try {
            return LinkUrlParser.parseGitHubRepo(url)
                    .map(repo -> githubClient.fetchLatestUpdates(repo.owner(), repo.repo(), since))
                    .orElse(List.of());
        } finally {
            metrics.recordGithubDuration(System.currentTimeMillis() - start);
        }
    }
}
