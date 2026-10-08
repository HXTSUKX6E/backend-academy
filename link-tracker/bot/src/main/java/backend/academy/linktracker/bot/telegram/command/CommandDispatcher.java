package backend.academy.linktracker.bot.telegram.command;

import backend.academy.linktracker.bot.metrics.BotMetrics;
import org.springframework.stereotype.Component;

@Component
public class CommandDispatcher {

    public static final String UNKNOWN =
            "Неизвестная команда. Воспользуйтесь /help, чтобы посмотреть список доступных команд.";

    private final CommandRegistry registry;
    private final BotMetrics botMetrics;

    public CommandDispatcher(CommandRegistry registry, BotMetrics botMetrics) {
        this.registry = registry;
        this.botMetrics = botMetrics;
    }

    public String dispatch(String text, long chatId) {
        String cmd = text.split("\\s+")[0];

        BotCommandHandler handler = registry.find(cmd);
        if (handler != null) {
            botMetrics.incrementCommandRequests(cmd);
            return handler.handle(chatId, text);
        }
        return UNKNOWN;
    }

    public boolean isKnownCommand(String text) {
        return registry.find(text.split("\\s+")[0]) != null;
    }
}
