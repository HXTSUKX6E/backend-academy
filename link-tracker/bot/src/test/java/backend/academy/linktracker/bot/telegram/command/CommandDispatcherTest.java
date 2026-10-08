package backend.academy.linktracker.bot.telegram.command;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.bot.metrics.BotMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

class CommandDispatcherTest {

    private CommandDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        BotCommandHandler start = new BotCommandHandler() {
            @Override
            public String command() {
                return "/start";
            }

            @Override
            public String description() {
                return "start";
            }

            @Override
            public String handle(long chatId, String text) {
                return "Добро пожаловать";
            }
        };

        BotCommandHandler help = new BotCommandHandler() {
            @Override
            public String command() {
                return "/help";
            }

            @Override
            public String description() {
                return "help";
            }

            @Override
            public String handle(long chatId, String text) {
                return "/start\n/help";
            }
        };

        CommandRegistry registry = new CommandRegistry(providerOf(List.of(start, help)));
        BotMetrics botMetrics = new BotMetrics(new SimpleMeterRegistry());
        dispatcher = new CommandDispatcher(registry, botMetrics);
    }

    @Test
    void startCommand_returnsWelcomeMessage() {
        String response = dispatcher.dispatch("/start", 1L);

        assertThat(response.toLowerCase()).contains("добро пожаловать");
    }

    @Test
    void helpCommand_containsCommands() {
        String response = dispatcher.dispatch("/help", 1L);

        assertThat(response).contains("/start").contains("/help");
    }

    @Test
    void unknownCommand_returnsErrorMessage() {
        String response = dispatcher.dispatch("/abracadabra", 1L);

        assertThat(response.toLowerCase()).contains("неизвестная команда");
    }

    private static <T> ObjectProvider<T> providerOf(List<T> items) {
        return new ObjectProvider<>() {
            @NotNull
            @Override
            public T getObject(@NotNull Object... args) {
                return items.getFirst();
            }

            @Override
            public T getIfAvailable() {
                return items.isEmpty() ? null : items.getFirst();
            }

            @Override
            public T getIfUnique() {
                return items.size() == 1 ? items.getFirst() : null;
            }

            @Override
            public java.util.stream.Stream<T> stream() {
                return items.stream();
            }

            @Override
            public java.util.stream.Stream<T> orderedStream() {
                return items.stream();
            }
        };
    }
}
