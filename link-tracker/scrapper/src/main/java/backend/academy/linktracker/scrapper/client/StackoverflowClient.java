package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.LinkUpdate.UpdateType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class StackoverflowClient {

    private static final String ANSWERS_URI =
            "/questions/{id}/answers?site=stackoverflow&sort=creation&order=desc&pagesize=100&filter=withbody&fromdate={fromdate}";
    private static final String QUESTION_URI = "/questions/{id}?site=stackoverflow";
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient stackoverflowRestClient;

    @Retry(name = "external")
    @CircuitBreaker(name = "stackoverflow")
    public List<LinkUpdate> fetchLatestAnswers(long questionId, Instant since) {
        Map<String, Object> answerResponse = stackoverflowRestClient
                .get()
                .uri(ANSWERS_URI, questionId, since.getEpochSecond())
                .retrieve()
                .body(MAP_TYPE);

        if (answerResponse == null) {
            return List.of();
        }
        Object itemsObj = answerResponse.get("items");
        if (!(itemsObj instanceof List<?> items) || items.isEmpty()) {
            return List.of();
        }

        String questionTitle = fetchQuestionTitle(questionId);
        return items.stream()
                .filter(i -> i instanceof Map<?, ?>)
                .map(i -> (Map<?, ?>) i)
                .map(answer -> buildUpdate(questionTitle, answer))
                .filter(u -> u.createdAt().isAfter(since))
                .toList();
    }

    private LinkUpdate buildUpdate(String questionTitle, Map<?, ?> answer) {
        Object creationObj = answer.get("creation_date");
        Instant createdAt = creationObj instanceof Number n ? Instant.ofEpochSecond(n.longValue()) : Instant.now();
        String author = answer.get("owner") instanceof Map<?, ?> owner ? (String) owner.get("display_name") : "";
        String rawBody = answer.containsKey("body_markdown")
                ? (String) answer.get("body_markdown")
                : (String) answer.get("body");
        return LinkUpdate.of(
                questionTitle,
                author != null ? author : "",
                createdAt,
                rawBody,
                createdAt,
                UpdateType.STACKOVERFLOW_ANSWER);
    }

    private String fetchQuestionTitle(long questionId) {
        try {
            Map<String, Object> body = stackoverflowRestClient
                    .get()
                    .uri(QUESTION_URI, questionId)
                    .retrieve()
                    .body(MAP_TYPE);
            if (body == null) {
                return "";
            }
            Object itemsObj = body.get("items");
            if (!(itemsObj instanceof List<?> items) || items.isEmpty()) {
                return "";
            }
            if (!(items.getFirst() instanceof Map<?, ?> question)) {
                return "";
            }
            Object title = question.get("title");
            return title instanceof String s ? s : "";
        } catch (Exception e) {
            log.atWarn().addKeyValue("questionId", questionId).setCause(e).log("Failed to fetch question title");
            return "";
        }
    }
}
