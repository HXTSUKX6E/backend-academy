package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.dto.AddLinkRequest;
import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.dto.RemoveLinkRequest;
import backend.academy.linktracker.bot.metrics.BotMetrics;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class ScrapperClient {

    private static final String TG_CHAT_HEADER = "Tg-Chat-Id";
    private static final String CB_NAME = "scrapper";
    private static final String SCOPE = "scrapper_sync_api";

    private final RestClient scrapperRestClient;
    private final BotMetrics botMetrics;

    @Retry(name = "scrapper")
    @CircuitBreaker(name = CB_NAME)
    public void registerChat(long chatId) {
        long start = System.currentTimeMillis();
        try {
            scrapperRestClient.post().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();
        } finally {
            botMetrics.recordCommandDuration(SCOPE, "registerChat", System.currentTimeMillis() - start);
        }
    }

    @Retry(name = "scrapper")
    @CircuitBreaker(name = CB_NAME)
    public LinkResponse addLink(long chatId, AddLinkRequest request) {
        long start = System.currentTimeMillis();
        try {
            return scrapperRestClient
                    .post()
                    .uri("/links")
                    .header(TG_CHAT_HEADER, String.valueOf(chatId))
                    .body(request)
                    .retrieve()
                    .body(LinkResponse.class);
        } finally {
            botMetrics.recordCommandDuration(SCOPE, "addLink", System.currentTimeMillis() - start);
        }
    }

    @Retry(name = "scrapper")
    @CircuitBreaker(name = CB_NAME)
    public LinkResponse removeLink(long chatId, RemoveLinkRequest request) {
        long start = System.currentTimeMillis();
        try {
            return scrapperRestClient
                    .method(org.springframework.http.HttpMethod.DELETE)
                    .uri("/links")
                    .header(TG_CHAT_HEADER, String.valueOf(chatId))
                    .body(request)
                    .retrieve()
                    .body(LinkResponse.class);
        } finally {
            botMetrics.recordCommandDuration(SCOPE, "removeLink", System.currentTimeMillis() - start);
        }
    }

    @Retry(name = "scrapper")
    @CircuitBreaker(name = CB_NAME)
    public ListLinksResponse listLinks(long chatId) {
        long start = System.currentTimeMillis();
        try {
            ListLinksResponse response = scrapperRestClient
                    .get()
                    .uri("/links")
                    .header(TG_CHAT_HEADER, String.valueOf(chatId))
                    .retrieve()
                    .body(ListLinksResponse.class);
            return Objects.requireNonNullElse(response, new ListLinksResponse(java.util.List.of(), 0));
        } finally {
            botMetrics.recordCommandDuration(SCOPE, "listLinks", System.currentTimeMillis() - start);
        }
    }
}
