package backend.academy.linktracker.scrapper.outbox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.bot", name = "protocol", havingValue = "kafka-outbox")
public class JdbcOutboxRepository implements OutboxRepository {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public void save(OutboxEvent event) {
        String sql = """
                INSERT INTO outbox_events (aggregate_id, event_type, payload)
                VALUES (:aggregateId, :eventType, :payload::jsonb)
                """.trim();
        jdbc.update(
                sql,
                Map.of(
                        "aggregateId", event.getAggregateId(),
                        "eventType", event.getEventType(),
                        "payload", event.getPayload()));
    }

    @Override
    public List<OutboxEvent> findUnpublished(int limit) {
        String sql = """
                SELECT id, aggregate_id, event_type, payload, created_at, retry_count
                FROM outbox_events
                WHERE published_at IS NULL
                ORDER BY created_at
                LIMIT :limit
                FOR UPDATE SKIP LOCKED
                """.trim();
        return jdbc.query(sql, Map.of("limit", limit), this::mapRow);
    }

    @Override
    public void markPublished(UUID id) {
        String sql = "UPDATE outbox_events SET published_at = NOW() WHERE id = :id";
        jdbc.update(sql, new MapSqlParameterSource("id", id));
    }

    @SuppressWarnings("PMD.UnusedFormalParameter")
    private OutboxEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        return OutboxEvent.builder()
                .id(UUID.fromString(rs.getString("id")))
                .aggregateId(rs.getLong("aggregate_id"))
                .eventType(rs.getString("event_type"))
                .payload(rs.getString("payload"))
                .createdAt(createdAt != null ? createdAt.toInstant() : Instant.now())
                .retryCount(rs.getInt("retry_count"))
                .build();
    }
}
