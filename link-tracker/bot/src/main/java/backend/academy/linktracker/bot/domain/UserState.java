package backend.academy.linktracker.bot.domain;

import java.util.List;

public record UserState(long chatId, ConversationState conversationState, String pendingUrl, List<String> pendingTags) {

    public static UserState idle(long chatId) {
        return new UserState(chatId, ConversationState.IDLE, null, List.of());
    }
}
