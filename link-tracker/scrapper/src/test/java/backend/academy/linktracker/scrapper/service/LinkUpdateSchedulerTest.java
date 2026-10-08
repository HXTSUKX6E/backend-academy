package backend.academy.linktracker.scrapper.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.domain.TrackedLink;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.LinkUpdate.UpdateType;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LinkUpdateSchedulerTest {

    private LinkRepository linkRepository;
    private LinkUpdateChecker checker;
    private LinkNotificationService notificationService;
    private TransactionalLinkUpdateService transactionalService;
    private SchedulerProperties props;

    @BeforeEach
    void setUp() {
        linkRepository = mock(LinkRepository.class);
        checker = mock(LinkUpdateChecker.class);
        notificationService = mock(LinkNotificationService.class);
        transactionalService = new TransactionalLinkUpdateService(linkRepository, notificationService);

        props = new SchedulerProperties();
        props.setBatchSize(50);
        props.setThreadCount(2);
        props.setDelay(60_000);
    }

    private LinkUpdateScheduler scheduler() {
        return new LinkUpdateScheduler(
                linkRepository,
                List.of(checker),
                notificationService,
                props,
                Executors.newFixedThreadPool(props.getThreadCount()),
                transactionalService);
    }

    @Test
    void sendsUpdateOnlyForChangedLinks() {
        TrackedLink ghLink = link(1L, "https://github.com/u/r");
        ghLink.addChat(100L);
        ghLink.addChat(101L);
        TrackedLink soLink = link(2L, "https://stackoverflow.com/questions/123/test");
        soLink.addChat(102L);

        when(linkRepository.getLinksAfter(0L, 50)).thenReturn(List.of(ghLink, soLink));
        when(linkRepository.getLinksAfter(2L, 50)).thenReturn(List.of());

        when(checker.supports("https://github.com/u/r")).thenReturn(true);
        when(checker.supports("https://stackoverflow.com/questions/123/test")).thenReturn(false);
        LinkUpdate update =
                LinkUpdate.of("New Issue", "alice", Instant.now(), "body text", Instant.now(), UpdateType.GITHUB_ISSUE);
        when(checker.check(eq("https://github.com/u/r"), any())).thenReturn(List.of(update));

        scheduler().checkLinks();

        verify(notificationService).sendUpdate(eq(ghLink), eq(update));
        verify(notificationService, never()).sendUpdate(eq(soLink), any());
    }

    @Test
    void doesNotSendUpdateWhenNoNewActivity() {
        TrackedLink link = link(1L, "https://github.com/u/r");
        link.addChat(42L);

        when(linkRepository.getLinksAfter(0L, 50)).thenReturn(List.of(link));
        when(linkRepository.getLinksAfter(1L, 50)).thenReturn(List.of());
        when(checker.supports(any())).thenReturn(true);
        when(checker.check(any(), any())).thenReturn(List.of());

        scheduler().checkLinks();

        verifyNoInteractions(notificationService);
    }

    @Test
    void unknownUrlIsSkippedWithoutError() {
        TrackedLink link = link(1L, "https://unknown.example.com/page");
        link.addChat(7L);

        when(linkRepository.getLinksAfter(0L, 50)).thenReturn(List.of(link));
        when(linkRepository.getLinksAfter(1L, 50)).thenReturn(List.of());
        when(checker.supports(any())).thenReturn(false);

        assertDoesNotThrow(() -> scheduler().checkLinks());
        verifyNoInteractions(notificationService);
    }

    @Test
    void checkerExceptionIsIsolatedAndFailureReportSent() {
        TrackedLink badLink = link(1L, "https://github.com/u/bad");
        badLink.addChat(10L);
        TrackedLink goodLink = link(2L, "https://github.com/u/good");
        goodLink.addChat(11L);

        when(linkRepository.getLinksAfter(0L, 50)).thenReturn(List.of(badLink, goodLink));
        when(linkRepository.getLinksAfter(2L, 50)).thenReturn(List.of());
        when(checker.supports(any())).thenReturn(true);
        when(checker.check(eq("https://github.com/u/bad"), any())).thenThrow(new RuntimeException("api error"));
        LinkUpdate update = LinkUpdate.of("Good", "bob", Instant.now(), "ok", Instant.now(), UpdateType.GITHUB_ISSUE);
        when(checker.check(eq("https://github.com/u/good"), any())).thenReturn(List.of(update));

        assertDoesNotThrow(() -> scheduler().checkLinks());

        verify(notificationService).sendFailureReport(eq("https://github.com/u/bad"), anyList());
        verify(notificationService).sendUpdate(eq(goodLink), eq(update));
    }

    @Test
    void batchPagination_cursorAdvancesCorrectly() {
        props.setBatchSize(2);

        TrackedLink link1 = link(1L, "https://github.com/u/r1");
        TrackedLink link2 = link(2L, "https://github.com/u/r2");
        TrackedLink link3 = link(3L, "https://github.com/u/r3");
        List.of(link1, link2, link3).forEach(l -> l.addChat(1L));

        when(linkRepository.getLinksAfter(0L, 2)).thenReturn(List.of(link1, link2));
        when(linkRepository.getLinksAfter(2L, 2)).thenReturn(List.of(link3));
        when(linkRepository.getLinksAfter(3L, 2)).thenReturn(List.of());
        when(checker.supports(any())).thenReturn(false);

        scheduler().checkLinks();

        verify(linkRepository).getLinksAfter(0L, 2);
        verify(linkRepository).getLinksAfter(2L, 2);
    }

    @Test
    void updateTimestampIsPersistedAfterUpdate() {
        TrackedLink link = link(1L, "https://github.com/u/r");
        link.addChat(5L);
        Instant newTs = Instant.now();
        LinkUpdate update = LinkUpdate.of("T", "u", newTs, "body", newTs, UpdateType.GITHUB_ISSUE);

        when(linkRepository.getLinksAfter(0L, 50)).thenReturn(List.of(link));
        when(linkRepository.getLinksAfter(1L, 50)).thenReturn(List.of());
        when(checker.supports(any())).thenReturn(true);
        when(checker.check(any(), any())).thenReturn(List.of(update));

        scheduler().checkLinks();

        verify(linkRepository).updateLastUpdatedAt(1L, newTs);
        verify(notificationService).sendUpdate(link, update);
    }

    @Test
    void firstSupportingCheckerIsUsed() {
        LinkUpdateChecker first = mock(LinkUpdateChecker.class);
        LinkUpdateChecker second = mock(LinkUpdateChecker.class);
        LinkUpdateScheduler sched = new LinkUpdateScheduler(
                linkRepository,
                List.of(first, second),
                notificationService,
                props,
                Executors.newFixedThreadPool(props.getThreadCount()),
                transactionalService);

        TrackedLink link = link(1L, "https://github.com/u/r");
        link.addChat(1L);

        when(linkRepository.getLinksAfter(0L, 50)).thenReturn(List.of(link));
        when(linkRepository.getLinksAfter(1L, 50)).thenReturn(List.of());
        when(first.supports(any())).thenReturn(true);
        when(first.check(any(), any())).thenReturn(List.of());

        sched.checkLinks();

        verify(first).check(any(), any());
        verify(second, never()).check(any(), any());
    }

    private static TrackedLink link(long id, String url) {
        return new TrackedLink(id, url, List.of(), List.of());
    }
}
