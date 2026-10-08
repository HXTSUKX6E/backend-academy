package backend.academy.linktracker.ai.service;

import java.util.List;

public record PrioritizedUpdate(long id, String url, String description, List<Long> tgChatIds, Priority priority) {}
