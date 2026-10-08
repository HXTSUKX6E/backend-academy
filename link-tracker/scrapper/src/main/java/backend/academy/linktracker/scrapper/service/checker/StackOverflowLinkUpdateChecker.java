package backend.academy.linktracker.scrapper.service.checker;

import backend.academy.linktracker.scrapper.client.StackoverflowClient;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.metrics.ScrapperMetrics;
import backend.academy.linktracker.scrapper.service.LinkUpdateChecker;
import backend.academy.linktracker.scrapper.util.LinkUrlParser;
import java.time.Instant;
import java.util.List;
import java.util.OptionalLong;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StackOverflowLinkUpdateChecker implements LinkUpdateChecker {

    private final StackoverflowClient stackoverflowClient;
    private final ScrapperMetrics metrics;

    @Override
    public boolean supports(String url) {
        return LinkUrlParser.parseStackOverflowQuestionId(url).isPresent();
    }

    @Override
    public List<LinkUpdate> check(String url, Instant since) {
        OptionalLong questionId = LinkUrlParser.parseStackOverflowQuestionId(url);
        if (questionId.isEmpty()) {
            return List.of();
        }
        long start = System.currentTimeMillis();
        try {
            return stackoverflowClient.fetchLatestAnswers(questionId.getAsLong(), since);
        } finally {
            metrics.recordStackoverflowDuration(System.currentTimeMillis() - start);
        }
    }
}
