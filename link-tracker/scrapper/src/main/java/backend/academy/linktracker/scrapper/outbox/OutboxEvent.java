package backend.academy.linktracker.scrapper.outbox;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {
    private UUID id;
    private long aggregateId;
    private String eventType;
    private String payload;
    private Instant createdAt;
    private Instant publishedAt;
    private int retryCount;
}
