package backend.academy.linktracker.scrapper.repository.impl.jdbc;

import backend.academy.linktracker.scrapper.domain.TrackedLink;
import backend.academy.linktracker.scrapper.exception.DuplicateLinkException;
import backend.academy.linktracker.scrapper.metrics.ScrapperMetrics;
import backend.academy.linktracker.scrapper.repository.ScrapperRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "app.database-access-type", havingValue = "sql", matchIfMissing = true)
public class JdbcScrapperRepository implements ScrapperRepository {

    private static final TrackedLinkRowMapper LINK_ROW_MAPPER = TrackedLinkRowMapper.INSTANCE;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ScrapperMetrics metrics;

    public JdbcScrapperRepository(NamedParameterJdbcTemplate jdbcTemplate, ScrapperMetrics metrics) {
        this.jdbcTemplate = jdbcTemplate;
        this.metrics = metrics;
    }

    @Override
    public boolean registerChat(long chatId) {
        if (chatExists(chatId)) {
            return false;
        }
        int inserted = jdbcTemplate.update(
                "insert into chats(id) values (:chatId)", new MapSqlParameterSource("chatId", chatId));
        return inserted > 0;
    }

    @Override
    public boolean deleteChat(long chatId) {
        int deleted = jdbcTemplate.update(
                "delete from chats where id = :chatId", new MapSqlParameterSource("chatId", chatId));
        return deleted > 0;
    }

    @Override
    public boolean chatExists(long chatId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from chats where id = :chatId",
                new MapSqlParameterSource("chatId", chatId),
                Integer.class);
        return count != null && count > 0;
    }

    @Override
    @Transactional
    public TrackedLink addLink(long chatId, String url, List<String> tags, List<String> filters) {
        Long linkId = upsertLink(url, tags, filters);

        try {
            jdbcTemplate.update(
                    """
                insert into chat_links(chat_id, link_id)
                values (:chatId, :linkId)
                """, new MapSqlParameterSource().addValue("chatId", chatId).addValue("linkId", linkId));
        } catch (DuplicateKeyException ex) {
            throw new DuplicateLinkException("already tracked");
        }

        return getLinkById(linkId);
    }

    @Override
    @Transactional
    public TrackedLink removeLink(long chatId, String url) {
        Long linkId = findLinkIdByUrl(url);
        if (linkId == null) {
            return null;
        }

        TrackedLink removed = getLinkById(linkId);

        int deleted = jdbcTemplate.update(
                "delete from chat_links where chat_id = :chatId and link_id = :linkId",
                new MapSqlParameterSource().addValue("chatId", chatId).addValue("linkId", linkId));
        if (deleted == 0) {
            return null;
        }

        Integer refs = jdbcTemplate.queryForObject(
                "select count(*) from chat_links where link_id = :linkId",
                new MapSqlParameterSource("linkId", linkId),
                Integer.class);
        if (refs != null && refs == 0) {
            jdbcTemplate.update("delete from links where id = :linkId", new MapSqlParameterSource("linkId", linkId));
        }

        if (removed != null) {
            removed.removeChat(chatId);
        }
        return removed;
    }

    @Override
    public List<TrackedLink> getLinksByChat(long chatId) {
        return findLinks("""
            select l.id, l.url, l.tags, l."filters", l.last_updated_at, cl.chat_id
            from links l
            join chat_links cl on cl.link_id = l.id
            where cl.chat_id = :chatId
            order by l.id
            """, new MapSqlParameterSource("chatId", chatId));
    }

    @Override
    public List<TrackedLink> getAllTrackedLinks() {
        return findLinks("""
            select l.id, l.url, l.tags, l."filters", l.last_updated_at, cl.chat_id
            from links l
            join chat_links cl on cl.link_id = l.id
            order by l.id
            """, new MapSqlParameterSource());
    }

    @Override
    public List<TrackedLink> getLinksAfter(long afterId, int limit) {
        long start = System.currentTimeMillis();
        try {
            return findLinks(
                    """
                select l.id, l.url, l.tags, l."filters", l.last_updated_at, cl.chat_id
                from links l
                join chat_links cl on cl.link_id = l.id
                where l.id in (
                    select id from links where id > :afterId order by id limit :limit
                )
                order by l.id
                """,
                    new MapSqlParameterSource().addValue("afterId", afterId).addValue("limit", limit));
        } finally {
            metrics.recordDbDuration(System.currentTimeMillis() - start);
        }
    }

    @Override
    public void updateLastUpdatedAt(long linkId, Instant lastUpdatedAt) {
        long start = System.currentTimeMillis();
        try {
            jdbcTemplate.update(
                    "update links set last_updated_at = :updatedAt where id = :linkId",
                    new MapSqlParameterSource()
                            .addValue("updatedAt", Timestamp.from(lastUpdatedAt))
                            .addValue("linkId", linkId));
        } finally {
            metrics.recordDbDuration(System.currentTimeMillis() - start);
        }
    }

    private Long upsertLink(String url, List<String> tags, List<String> filters) {
        List<String> normalizedTags = tags == null ? List.of() : tags;
        List<String> normalizedFilters = filters == null ? List.of() : filters;

        Long existingId = findLinkIdByUrl(url);
        if (existingId != null) {
            jdbcTemplate.update(
                    "update links set tags = :tags, \"filters\" = :filters where id = :id",
                    new MapSqlParameterSource()
                            .addValue("id", existingId)
                            .addValue("tags", String.join(",", normalizedTags))
                            .addValue("filters", String.join(",", normalizedFilters)));
            return existingId;
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                """
            insert into links(url, tags, "filters", last_updated_at)
            values (:url, :tags, :filters, :lastUpdatedAt)
            """,
                new MapSqlParameterSource()
                        .addValue("url", url)
                        .addValue("tags", String.join(",", normalizedTags))
                        .addValue("filters", String.join(",", normalizedFilters))
                        .addValue("lastUpdatedAt", Timestamp.from(Instant.EPOCH)),
                keyHolder,
                new String[] {"id"});

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new DuplicateLinkException("already tracked");
        }
        return key.longValue();
    }

    private Long findLinkIdByUrl(String url) {
        return jdbcTemplate
                .query(
                        "select id from links where url = :url",
                        new MapSqlParameterSource("url", url),
                        (rs, rowNum) -> rs.getLong("id"))
                .stream()
                .findFirst()
                .orElse(null);
    }

    private TrackedLink getLinkById(Long linkId) {
        return findLinks("""
            select l.id, l.url, l.tags, l."filters", l.last_updated_at, cl.chat_id
            from links l
            left join chat_links cl on cl.link_id = l.id
            where l.id = :linkId
            """, new MapSqlParameterSource("linkId", linkId)).stream()
                .findFirst()
                .orElse(null);
    }

    private List<TrackedLink> findLinks(String sql, MapSqlParameterSource params) {
        List<TrackedLinkRow> rows = jdbcTemplate.query(sql, params, LINK_ROW_MAPPER);
        Map<Long, TrackedLink> grouped = new LinkedHashMap<>();
        for (TrackedLinkRow row : rows) {
            TrackedLink link = grouped.computeIfAbsent(
                    row.id(),
                    id -> new TrackedLink(
                            id,
                            row.url(),
                            TrackedLinkRowMapper.splitCsv(row.tags()),
                            TrackedLinkRowMapper.splitCsv(row.filters()),
                            row.lastUpdatedAt()));
            if (row.chatId() != null) {
                link.addChat(row.chatId());
            }
        }
        return new ArrayList<>(grouped.values());
    }
}
