package backend.academy.linktracker.bot.telegram.command;

import backend.academy.linktracker.bot.domain.ConversationState;
import backend.academy.linktracker.bot.domain.UserState;
import backend.academy.linktracker.bot.repository.UserStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UntrackCommandHandler implements BotCommandHandler {

    private final UserStateRepository repository;

    @Override
    public String command() {
        return "/untrack";
    }

    @Override
    public String description() {
        return "убрать ссылку из отслеживания";
    }

    @Override
    public String handle(long chatId, String text) {
        repository.save(new UserState(chatId, ConversationState.WAITING_UNTRACK_URL, null, java.util.List.of()));
        return "Отправьте ссылку, которую нужно убрать из отслеживания";
    }
}
