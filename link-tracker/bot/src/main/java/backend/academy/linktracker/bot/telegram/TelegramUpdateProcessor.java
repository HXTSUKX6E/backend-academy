package backend.academy.linktracker.bot.telegram;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.domain.ConversationState;
import backend.academy.linktracker.bot.domain.UserState;
import backend.academy.linktracker.bot.dto.AddLinkRequest;
import backend.academy.linktracker.bot.dto.RemoveLinkRequest;
import backend.academy.linktracker.bot.metrics.BotMetrics;
import backend.academy.linktracker.bot.repository.UserStateRepository;
import backend.academy.linktracker.bot.telegram.command.CommandDispatcher;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Component
public class TelegramUpdateProcessor {

    private final TelegramBot bot;
    private final CommandDispatcher dispatcher;
    private final UserStateRepository userStateRepository;
    private final ScrapperClient scrapperClient;
    private final BotMetrics botMetrics;
    private final Map<ConversationState, ConversationHandler> conversationHandlers;

    public TelegramUpdateProcessor(
            TelegramBot bot,
            CommandDispatcher dispatcher,
            UserStateRepository userStateRepository,
            ScrapperClient scrapperClient,
            BotMetrics botMetrics) {
        this.bot = bot;
        this.dispatcher = dispatcher;
        this.userStateRepository = userStateRepository;
        this.scrapperClient = scrapperClient;
        this.botMetrics = botMetrics;
        this.conversationHandlers = createConversationHandlers();
    }

    public void process(Update update) {
        Message msg = update.message();
        if (msg == null || msg.text() == null) {
            return;
        }

        long chatId = msg.chat().id();
        String text = msg.text().trim();

        botMetrics.incrementTelegramRequests(text.startsWith("/"));

        try {
            String reply;
            if (text.startsWith("/")) {
                UserState state = userStateRepository.findByChatId(chatId).orElse(UserState.idle(chatId));
                if (state.conversationState() != ConversationState.IDLE
                        && !text.startsWith("/cancel")
                        && !dispatcher.isKnownCommand(text)) {
                    reply = CommandDispatcher.UNKNOWN;
                } else {
                    if (state.conversationState() != ConversationState.IDLE && !text.startsWith("/cancel")) {
                        userStateRepository.save(UserState.idle(chatId));
                    }
                    reply = dispatcher.dispatch(text, chatId);
                }
            } else {
                reply = processConversation(chatId, text);
            }

            log.atInfo().addKeyValue("chatId", chatId).addKeyValue("text", text).log("Incoming message");

            bot.execute(new SendMessage(chatId, reply));
        } catch (Exception e) {
            log.error("Failed to process telegram update for chatId={}", chatId, e);
            bot.execute(new SendMessage(chatId, "Произошла ошибка, попробуйте позже"));
        }
    }

    private String processConversation(long chatId, String text) {
        UserState state = userStateRepository.findByChatId(chatId).orElse(UserState.idle(chatId));

        ConversationHandler handler = conversationHandlers.getOrDefault(
                state.conversationState(), (id, messageText, userState) -> CommandDispatcher.UNKNOWN);

        return handler.handle(chatId, text, state);
    }

    private Map<ConversationState, ConversationHandler> createConversationHandlers() {
        Map<ConversationState, ConversationHandler> handlers = new EnumMap<>(ConversationState.class);
        handlers.put(ConversationState.WAITING_TRACK_URL, (chatId, text, state) -> handleTrackUrl(chatId, text));
        handlers.put(ConversationState.WAITING_TRACK_TAGS, this::handleTrackTags);
        handlers.put(ConversationState.WAITING_UNTRACK_URL, (chatId, text, state) -> handleUntrack(chatId, text));
        handlers.put(ConversationState.IDLE, (chatId, text, state) -> CommandDispatcher.UNKNOWN);
        return Map.copyOf(handlers);
    }

    private String handleTrackUrl(long chatId, String text) {
        if (!isValidUrl(text)) {
            return "Некорректная ссылка";
        }

        userStateRepository.save(new UserState(chatId, ConversationState.WAITING_TRACK_TAGS, text, List.of()));
        return "Введите теги через запятую (или '-' чтобы пропустить)";
    }

    private String handleTrackTags(long chatId, String text, UserState state) {
        List<String> tags = "-".equals(text)
                ? List.of()
                : Arrays.stream(text.split(","))
                        .map(String::trim)
                        .filter(it -> !it.isBlank())
                        .toList();

        try {
            scrapperClient.addLink(chatId, new AddLinkRequest(state.pendingUrl(), tags, List.of()));
            userStateRepository.save(UserState.idle(chatId));
            return "Ссылка добавлена в отслеживание";
        } catch (HttpClientErrorException.Conflict e) {
            userStateRepository.save(UserState.idle(chatId));
            return "Ссылка уже отслеживается";
        }
    }

    private String handleUntrack(long chatId, String text) {
        if (!isValidUrl(text)) {
            return "Некорректная ссылка";
        }

        scrapperClient.removeLink(chatId, new RemoveLinkRequest(text));
        userStateRepository.save(UserState.idle(chatId));
        return "Ссылка удалена из отслеживания";
    }

    private boolean isValidUrl(String value) {
        try {
            URI uri = URI.create(value);
            return uri.getScheme() == null || (!"http".equals(uri.getScheme()) && !"https".equals(uri.getScheme()));
        } catch (Exception e) {
            return true;
        }
    }

    @FunctionalInterface
    private interface ConversationHandler {
        String handle(long chatId, String text, UserState state);
    }
}
