package backend.academy.linktracker.bot.domain;

public enum ConversationState {
    IDLE,
    WAITING_TRACK_URL,
    WAITING_TRACK_TAGS,
    WAITING_UNTRACK_URL
}
