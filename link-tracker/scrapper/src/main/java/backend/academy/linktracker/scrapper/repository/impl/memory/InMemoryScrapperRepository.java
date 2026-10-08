package backend.academy.linktracker.scrapper.repository.impl.memory;

import backend.academy.linktracker.scrapper.domain.TrackedLink;
import backend.academy.linktracker.scrapper.repository.ScrapperRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.database-access-type", havingValue = "memory")
public class InMemoryScrapperRepository implements ScrapperRepository {

    private final Set<Long> chats = ConcurrentHashMap.newKeySet();
    private final Map<String, TrackedLink> linksByUrl = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> linksByChat = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0L);

    @Override
    public boolean registerChat(long chatId) {
        linksByChat.putIfAbsent(chatId, ConcurrentHashMap.newKeySet());
        return chats.add(chatId);
    }

    @Override
    public boolean deleteChat(long chatId) {
        if (!chats.remove(chatId)) {
            return false;
        }
        Set<String> urls = linksByChat.remove(chatId);
        if (urls != null) {
            for (String url : urls) {
                TrackedLink link = linksByUrl.get(url);
                if (link != null) {
                    link.removeChat(chatId);
                    if (link.getChatIds().isEmpty()) {
                        linksByUrl.remove(url);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean chatExists(long chatId) {
        return chats.contains(chatId);
    }

    @Override
    public TrackedLink addLink(long chatId, String url, List<String> tags, List<String> filters) {
        TrackedLink link = linksByUrl.computeIfAbsent(
                url,
                key -> new TrackedLink(
                        idGenerator.incrementAndGet(),
                        key,
                        tags == null ? List.of() : tags,
                        filters == null ? List.of() : filters));

        Set<String> chatLinks = linksByChat.computeIfAbsent(chatId, it -> ConcurrentHashMap.newKeySet());
        if (!chatLinks.add(url)) {
            throw new IllegalStateException("already tracked");
        }
        link.addChat(chatId);
        return link;
    }

    @Override
    public TrackedLink removeLink(long chatId, String url) {
        Set<String> chatLinks = linksByChat.get(chatId);
        if (chatLinks == null || !chatLinks.remove(url)) {
            return null;
        }
        TrackedLink link = linksByUrl.get(url);
        if (link != null) {
            link.removeChat(chatId);
            if (link.getChatIds().isEmpty()) {
                linksByUrl.remove(url);
            }
        }
        return link;
    }

    @Override
    public List<TrackedLink> getLinksByChat(long chatId) {
        Set<String> urls = linksByChat.getOrDefault(chatId, Set.of());
        List<TrackedLink> result = new ArrayList<>();
        for (String url : urls) {
            TrackedLink link = linksByUrl.get(url);
            if (link != null) {
                result.add(link);
            }
        }
        return result;
    }

    @Override
    public List<TrackedLink> getAllTrackedLinks() {
        return new ArrayList<>(linksByUrl.values());
    }

    @Override
    public List<TrackedLink> getLinksAfter(long afterId, int limit) {
        return linksByUrl.values().stream()
                .filter(link -> link.getId() > afterId)
                .sorted(java.util.Comparator.comparingLong(TrackedLink::getId))
                .limit(limit)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void updateLastUpdatedAt(long linkId, Instant lastUpdatedAt) {
        linksByUrl.values().stream()
                .filter(link -> link.getId() == linkId)
                .findFirst()
                .ifPresent(link -> link.updateTimestamp(lastUpdatedAt));
    }
}
