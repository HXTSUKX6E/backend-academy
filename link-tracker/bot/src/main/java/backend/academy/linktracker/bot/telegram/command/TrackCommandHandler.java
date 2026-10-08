package backend.academy.linktracker.bot.telegram.command;

import backend.academy.linktracker.bot.domain.ConversationState;
import backend.academy.linktracker.bot.domain.UserState;
import backend.academy.linktracker.bot.repository.UserStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrackCommandHandler implements BotCommandHandler {

    private final UserStateRepository repository;

    @Override
    public String command() {
        return "/track";
    }

    @Override
    public String description() {
        return "добавить ссылку в отслеживание";
    }

    @Override
    public String handle(long chatId, String text) {
        repository.save(new UserState(chatId, ConversationState.WAITING_TRACK_URL, null, java.util.List.of()));
        return "Отправьте ссылку для отслеживания";
    }
}
