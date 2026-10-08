package backend.academy.linktracker.bot.telegram.command;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.domain.UserState;
import backend.academy.linktracker.bot.repository.UserStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartCommandHandler implements BotCommandHandler {

    private final UserStateRepository repo;
    private final ScrapperClient scrapperClient;

    @Override
    public String command() {
        return "/start";
    }

    @Override
    public String description() {
        return "начало работы пользователя";
    }

    @Override
    public String handle(long chatId, String text) {
        boolean first = repo.findByChatId(chatId).isEmpty();
        repo.save(UserState.idle(chatId));
        try {
            scrapperClient.registerChat(chatId);
        } catch (Exception e) {
            log.atWarn().addKeyValue("chatId", chatId).setCause(e).log("Failed to register chat in scrapper");
        }

        if (first) {
            return "Добро пожаловать! Используйте /help, чтобы посмотреть доступные команды.";
        }
        return "С возвращением! Используйте /help, чтобы посмотреть доступные команды.";
    }
}
