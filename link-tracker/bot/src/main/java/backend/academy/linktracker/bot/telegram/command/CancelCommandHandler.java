package backend.academy.linktracker.bot.telegram.command;

import backend.academy.linktracker.bot.domain.UserState;
import backend.academy.linktracker.bot.repository.UserStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CancelCommandHandler implements BotCommandHandler {

    private final UserStateRepository repository;

    @Override
    public String command() {
        return "/cancel";
    }

    @Override
    public String description() {
        return "отменить текущий диалог";
    }

    @Override
    public String handle(long chatId, String text) {
        repository.save(UserState.idle(chatId));
        return "Действие отменено";
    }
}
