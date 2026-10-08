package backend.academy.linktracker.scrapper.domain;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;

@Getter
public class TrackedLink {
    private final long id;
    private final String url;
    private final List<String> tags;
    private final List<String> filters;
    private final Set<Long> chatIds = new HashSet<>();
    private Instant lastUpdatedAt;

    public TrackedLink(long id, String url, List<String> tags, List<String> filters) {
        this(id, url, tags, filters, Instant.EPOCH);
    }

    public TrackedLink(long id, String url, List<String> tags, List<String> filters, Instant lastUpdatedAt) {
        this.id = id;
        this.url = url;
        this.tags = tags;
        this.filters = filters;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public void addChat(long chatId) {
        chatIds.add(chatId);
    }

    public void removeChat(long chatId) {
        chatIds.remove(chatId);
    }

    public void updateTimestamp(Instant instant) {
        this.lastUpdatedAt = instant;
    }
}
