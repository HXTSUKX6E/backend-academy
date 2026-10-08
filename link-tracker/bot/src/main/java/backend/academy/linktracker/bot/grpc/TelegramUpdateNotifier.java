package backend.academy.linktracker.bot.grpc;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.metrics.BotMetrics;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramUpdateNotifier implements UpdateNotifier {

    private final TelegramBot bot;
    private final BotMetrics botMetrics;

    @Override
    public void notifyUpdate(LinkUpdate update) {
        List<Long> chatIds = update.tgChatIds();

        for (Long chatId : chatIds) {
            try {
                bot.execute(new SendMessage(chatId.longValue(), "Обнаружено обновление: " + update.url()));
                botMetrics.incrementSentNotifications();
            } catch (Exception e) {
                log.atWarn()
                        .addKeyValue("chatId", chatId)
                        .addKeyValue("link", update.url())
                        .setCause(e)
                        .log("Failed to send Telegram notification");
            }
        }

        log.atInfo()
                .addKeyValue("link", update.url())
                .addKeyValue("chatCount", chatIds.size())
                .log("gRPC update sent");
    }
}
