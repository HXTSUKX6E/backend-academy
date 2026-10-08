package backend.academy.linktracker.scrapper.dto;

import java.util.List;

public record LinkUpdateRequest(long id, String url, String description, List<Long> tgChatIds, String author) {
    public LinkUpdateRequest(long id, String url, String description, List<Long> tgChatIds) {
        this(id, url, description, tgChatIds, null);
    }
}
