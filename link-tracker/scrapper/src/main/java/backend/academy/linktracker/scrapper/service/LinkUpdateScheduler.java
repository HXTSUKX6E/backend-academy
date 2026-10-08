package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.TrackedLink;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LinkUpdateScheduler {

    private final LinkRepository linkRepository;
    private final List<LinkUpdateChecker> checkers;
    private final LinkNotificationService notificationService;
    private final SchedulerProperties schedulerProperties;
    private final ExecutorService executorService;
    private final TransactionalLinkUpdateService transactionalService;

    public LinkUpdateScheduler(
            LinkRepository linkRepository,
            List<LinkUpdateChecker> checkers,
            LinkNotificationService notificationService,
            SchedulerProperties schedulerProperties,
            ExecutorService executorService,
            TransactionalLinkUpdateService transactionalService) {
        this.linkRepository = linkRepository;
        this.checkers = checkers;
        this.notificationService = notificationService;
        this.schedulerProperties = schedulerProperties;
        this.executorService = executorService;
        this.transactionalService = transactionalService;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.delay:60000}")
    public void checkLinks() {
        long cursor = 0L;
        int batchSize = schedulerProperties.getBatchSize();
        int totalProcessed = 0;
        List<TrackedLink> batch;

        do {
            batch = linkRepository.getLinksAfter(cursor, batchSize);
            if (batch.isEmpty()) {
                break;
            }
            processBatchParallel(batch);
            totalProcessed += batch.size();
            cursor = batch.getLast().getId();
        } while (batch.size() == batchSize);

        log.atDebug().addKeyValue("totalProcessed", totalProcessed).log("Link check cycle complete");
    }

    private void processBatchParallel(List<TrackedLink> batch) {
        int threadCount = schedulerProperties.getThreadCount();
        int subBatchSize = (int) Math.ceil((double) batch.size() / threadCount);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < batch.size(); i += subBatchSize) {
            List<TrackedLink> subBatch = List.copyOf(batch.subList(i, Math.min(i + subBatchSize, batch.size())));
            futures.add(CompletableFuture.runAsync(() -> processSubBatch(subBatch), executorService));
        }

        try {
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        } catch (Exception e) {
            log.atWarn().setCause(e).log("Batch processing encountered an error");
        }
    }

    private void processSubBatch(List<TrackedLink> subBatch) {
        for (TrackedLink link : subBatch) {
            try {
                processLink(link);
            } catch (Exception e) {
                log.atWarn()
                        .addKeyValue("url", link.getUrl())
                        .setCause(e)
                        .log("Failed to process link — sending failure report");
                notificationService.sendFailureReport(link.getUrl(), new ArrayList<>(link.getChatIds()));
            }
        }
    }

    private void processLink(TrackedLink link) {
        List<LinkUpdate> updates = checkers.stream()
                .filter(checker -> checker.supports(link.getUrl()))
                .findFirst()
                .map(checker -> checker.check(link.getUrl(), link.getLastUpdatedAt()))
                .orElse(List.of());

        if (updates.isEmpty()) {
            return;
        }
        Instant maxTimestamp = updates.stream()
                .map(LinkUpdate::newTimestamp)
                .max(Comparator.naturalOrder())
                .orElseThrow();
        transactionalService.updateAndNotify(link, updates, maxTimestamp);
    }
}
