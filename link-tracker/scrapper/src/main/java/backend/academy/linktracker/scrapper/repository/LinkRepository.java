package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.TrackedLink;
import java.time.Instant;
import java.util.List;

public interface LinkRepository {

    TrackedLink addLink(long chatId, String url, List<String> tags, List<String> filters);

    TrackedLink removeLink(long chatId, String url);

    List<TrackedLink> getLinksByChat(long chatId);

    List<TrackedLink> getAllTrackedLinks();

    List<TrackedLink> getLinksAfter(long afterId, int limit);

    void updateLastUpdatedAt(long linkId, Instant lastUpdatedAt);
}
