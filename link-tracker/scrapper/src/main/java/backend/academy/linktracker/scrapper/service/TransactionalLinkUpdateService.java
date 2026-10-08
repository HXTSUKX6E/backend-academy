package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.TrackedLink;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionalLinkUpdateService {

    private final LinkRepository linkRepository;
    private final LinkNotificationService notificationService;

    @Transactional
    public void updateAndNotify(TrackedLink link, List<LinkUpdate> updates, Instant maxTimestamp) {
        linkRepository.updateLastUpdatedAt(link.getId(), maxTimestamp);
        updates.forEach(u -> notificationService.sendUpdate(link, u));
    }
}
