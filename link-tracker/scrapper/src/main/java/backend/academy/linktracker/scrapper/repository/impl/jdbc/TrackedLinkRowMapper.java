package backend.academy.linktracker.scrapper.repository.impl.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;

class TrackedLinkRowMapper implements RowMapper<TrackedLinkRow> {

    static final TrackedLinkRowMapper INSTANCE = new TrackedLinkRowMapper();

    @Override
    public TrackedLinkRow mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new TrackedLinkRow(
                rs.getLong("id"),
                rs.getString("url"),
                rs.getString("tags"),
                rs.getString("filters"),
                rs.getTimestamp("last_updated_at").toInstant(),
                (Long) rs.getObject("chat_id"));
    }

    static List<String> splitCsv(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        String[] parts = value.split(",");
        List<String> out = new ArrayList<>(parts.length);
        for (String part : parts) {
            if (!part.isBlank()) {
                out.add(part.trim());
            }
        }
        return out;
    }
}
