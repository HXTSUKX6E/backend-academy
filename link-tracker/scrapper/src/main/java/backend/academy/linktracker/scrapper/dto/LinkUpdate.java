package backend.academy.linktracker.scrapper.dto;

import java.time.Instant;

public record LinkUpdate(
        String title, String author, Instant createdAt, String bodyPreview, Instant newTimestamp, UpdateType type) {

    private static final int MAX_PREVIEW = 200;

    public static LinkUpdate of(
            String title, String author, Instant createdAt, String rawBody, Instant newTimestamp, UpdateType type) {
        String preview = rawBody == null ? "" : rawBody.substring(0, Math.min(rawBody.length(), MAX_PREVIEW));
        return new LinkUpdate(title, author, createdAt, preview, newTimestamp, type);
    }

    public enum UpdateType {
        GITHUB_ISSUE("Новый PR/Issue!"),
        STACKOVERFLOW_ANSWER("Новый ответ SO!");

        private final String label;

        UpdateType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
