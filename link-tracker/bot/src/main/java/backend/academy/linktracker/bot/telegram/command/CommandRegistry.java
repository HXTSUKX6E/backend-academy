package backend.academy.linktracker.bot.telegram.command;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class CommandRegistry {

    private final Map<String, BotCommandHandler> byCommand;

    public CommandRegistry(ObjectProvider<BotCommandHandler> handlersProvider) {
        this.byCommand = handlersProvider
                .orderedStream()
                .collect(Collectors.toUnmodifiableMap(BotCommandHandler::command, Function.identity(), (a, b) -> a));
    }

    public BotCommandHandler find(String command) {
        return byCommand.get(command);
    }

    public Collection<BotCommandHandler> all() {
        return byCommand.values();
    }
}
