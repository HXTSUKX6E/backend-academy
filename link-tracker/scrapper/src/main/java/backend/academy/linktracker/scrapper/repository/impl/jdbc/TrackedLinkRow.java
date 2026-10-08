package backend.academy.linktracker.scrapper.repository.impl.jdbc;

import java.time.Instant;

record TrackedLinkRow(long id, String url, String tags, String filters, Instant lastUpdatedAt, Long chatId) {}
