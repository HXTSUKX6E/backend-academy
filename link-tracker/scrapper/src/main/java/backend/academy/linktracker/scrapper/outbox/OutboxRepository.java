package backend.academy.linktracker.scrapper.outbox;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository {

    void save(OutboxEvent event);

    List<OutboxEvent> findUnpublished(int limit);

    void markPublished(UUID id);
}
