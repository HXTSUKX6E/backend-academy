package backend.academy.linktracker.bot.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.telegram.polling.enabled", havingValue = "true", matchIfMissing = true)
public class TelegramLongPollingRunner {

    private final TelegramBot bot;
    private final TelegramUpdateProcessor processor;

    @PostConstruct
    public void start() {
        bot.execute(
                new SetMyCommands(new BotCommand("/start", "начало работы"), new BotCommand("/help", "список команд")));
        bot.setUpdatesListener(this::onUpdates, e -> {
            log.atError()
                    .addKeyValue("exception", e.getClass().getName())
                    .addKeyValue("message", e.getMessage())
                    .setCause(e)
                    .log("Telegram updates listener error");
        });
        log.info("Telegram long polling started");
    }

    private int onUpdates(List<Update> updates) {
        for (Update upd : updates) {
            processor.process(upd);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
