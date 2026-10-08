package backend.academy.linktracker.bot.telegram.command;

import java.util.Comparator;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HelpCommandHandler implements BotCommandHandler {

    private final ObjectProvider<BotCommandHandler> handlersProvider;

    @Override
    public String command() {
        return "/help";
    }

    @Override
    public String description() {
        return "вывод списка доступных команд";
    }

    @Override
    public String handle(long chatId, String text) {
        return handlersProvider
                .orderedStream()
                .sorted(Comparator.comparing(BotCommandHandler::command))
                .map(handler -> handler.command() + " - " + handler.description())
                .collect(Collectors.joining("\n"));
    }
}
