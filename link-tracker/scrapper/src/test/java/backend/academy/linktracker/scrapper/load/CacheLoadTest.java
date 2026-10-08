package backend.academy.linktracker.scrapper.load;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.TestcontainersConfiguration;
import io.lettuce.core.support.caching.CacheFrontend;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@Tag("load")
@Slf4j
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = "app.cache.client-side-enabled=true")
class CacheLoadTest {

    private static final int WARMUP = 10;
    private static final int SAMPLES = 200;
    private static final long CHAT_ID = 9000L;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    CacheFrontend<String, byte[]> linksCacheFrontend;

    @BeforeAll
    void setUp() throws Exception {
        mockMvc.perform(post("/tg-chat/" + CHAT_ID)).andExpect(status().isOk());
        for (int i = 0; i < 20; i++) {
            mockMvc.perform(post("/links")
                            .header("Tg-Chat-Id", CHAT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format(
                                    "{\"link\":\"https://github.com/load/repo-%d\",\"tags\":[],\"filters\":[]}", i)))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void reportCacheLatencyComparison() throws Exception {
        long[] noCache = measureNoCache();
        long[] l2Cache = measureL2Cache();
        long[] l1Cache = measureL1Cache();

        String report = buildReport(noCache, l2Cache, l1Cache);
        log.info("\n{}", report);
        writeReport(report);
    }

    private long[] measureNoCache() throws Exception {
        for (int i = 0; i < WARMUP; i++) {
            evictCache();
            getLinks();
        }
        long[] times = new long[SAMPLES];
        for (int i = 0; i < SAMPLES; i++) {
            evictCache();
            long start = System.nanoTime();
            getLinks();
            times[i] = System.nanoTime() - start;
        }
        return times;
    }

    private long[] measureL2Cache() throws Exception {
        evictCache();
        getLinks();

        for (int i = 0; i < WARMUP; i++) {
            getLinks();
        }
        long[] times = new long[SAMPLES];
        for (int i = 0; i < SAMPLES; i++) {
            long start = System.nanoTime();
            getLinks();
            times[i] = System.nanoTime() - start;
        }
        return times;
    }

    private long[] measureL1Cache() throws Exception {
        evictCache();
        getLinks();
        linksCacheFrontend.get("links::" + CHAT_ID);

        for (int i = 0; i < WARMUP; i++) {
            getLinks();
        }
        long[] times = new long[SAMPLES];
        for (int i = 0; i < SAMPLES; i++) {
            long start = System.nanoTime();
            getLinks();
            times[i] = System.nanoTime() - start;
        }
        return times;
    }

    private void getLinks() throws Exception {
        mockMvc.perform(get("/links").header("Tg-Chat-Id", CHAT_ID)).andExpect(status().isOk());
    }

    private void evictCache() {
        var cache = cacheManager.getCache("links");
        if (cache != null) {
            cache.evict(CHAT_ID);
        }
    }

    private static double avgMs(long[] ns) {
        return Arrays.stream(ns).average().orElse(0) / 1_000_000.0;
    }

    private static double percentileMs(long[] ns, int p) {
        long[] sorted = ns.clone();
        Arrays.sort(sorted);
        int idx = (int) Math.ceil(p / 100.0 * sorted.length) - 1;
        return sorted[Math.max(0, idx)] / 1_000_000.0;
    }

    private static double minMs(long[] ns) {
        return Arrays.stream(ns).min().orElse(0) / 1_000_000.0;
    }

    private static double maxMs(long[] ns) {
        return Arrays.stream(ns).max().orElse(0) / 1_000_000.0;
    }

    private static String buildReport(long[] noCache, long[] l2, long[] l1) {
        return String.format(
                Locale.US,
                """
                # Отчёт о производительности

                **Конфигурация**: 20 ссылок на чат, %d прогревочных запросов, %d измеряемых запросов.

                | Сценарий              | Avg (мс) | p50 (мс) | p95 (мс) | p99 (мс) | Min (мс) | Max (мс) |
                |-----------------------|----------|----------|----------|----------|----------|----------|
                | Без кэша (БД)         | %8.3f | %8.3f | %8.3f | %8.3f | %8.3f | %8.3f |
                | Кэш Redis L2          | %8.3f | %8.3f | %8.3f | %8.3f | %8.3f | %8.3f |
                | CSC L1 (клиентский)   | %8.3f | %8.3f | %8.3f | %8.3f | %8.3f | %8.3f |

                ## Ускорение

                - Redis L2 vs без кэша:  **%.1fx быстрее** (avg)
                - CSC L1 vs Redis L2:    **%.1fx быстрее** (avg)
                - CSC L1 vs без кэша:    **%.1fx быстрее** (avg)

                ## Выводы

                - **Кэш Redis L2** устраняет обращения к базе данных, снижая задержку для рабочих нагрузок с преобладанием чтения.
                - **Client-Side Caching (L1)** полностью убирает сетевой round-trip до Redis,
                  отдавая ответы из локального ConcurrentHashMap. Обеспечивает минимальную
                  возможную задержку для «горячих» ключей и защищает Valkey от шквала запросов.
                - **Инвалидация кэша** (при добавлении/удалении ссылок) происходит автоматически:
                  Valkey рассылает сообщения CLIENT TRACKING BCAST, поддерживая консистентность
                  L1-кэша.
                - L1-кэш наиболее эффективен, когда один и тот же chat ID запрашиваем часто.
                """,
                WARMUP,
                SAMPLES,
                avgMs(noCache),
                percentileMs(noCache, 50),
                percentileMs(noCache, 95),
                percentileMs(noCache, 99),
                minMs(noCache),
                maxMs(noCache),
                avgMs(l2),
                percentileMs(l2, 50),
                percentileMs(l2, 95),
                percentileMs(l2, 99),
                minMs(l2),
                maxMs(l2),
                avgMs(l1),
                percentileMs(l1, 50),
                percentileMs(l1, 95),
                percentileMs(l1, 99),
                minMs(l1),
                maxMs(l1),
                avgMs(noCache) / avgMs(l2),
                avgMs(l2) / avgMs(l1),
                avgMs(noCache) / avgMs(l1));
    }

    private static void writeReport(String content) {
        Path target = Path.of("target", "cache-performance-report.md");
        try {
            Files.createDirectories(target.getParent());
            Files.writeString(target, content, StandardCharsets.UTF_8);
            log.info("Report written to {}", target.toAbsolutePath());
        } catch (IOException e) {
            log.warn("Could not write report file: {}", e.getMessage());
        }
    }
}
