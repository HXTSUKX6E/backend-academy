package backend.academy.linktracker.bot.telegram;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.domain.ConversationState;
import backend.academy.linktracker.bot.domain.UserState;
import backend.academy.linktracker.bot.dto.AddLinkRequest;
import backend.academy.linktracker.bot.metrics.BotMetrics;
import backend.academy.linktracker.bot.repository.InMemoryUserStateRepository;
import backend.academy.linktracker.bot.repository.UserStateRepository;
import backend.academy.linktracker.bot.telegram.command.CommandDispatcher;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

class TelegramUpdateProcessorTest {

    private TelegramBot bot;
    private CommandDispatcher dispatcher;
    private UserStateRepository stateRepository;
    private ScrapperClient scrapperClient;
    private TelegramUpdateProcessor processor;

    @BeforeEach
    void setUp() {
        bot = mock(TelegramBot.class);
        dispatcher = mock(CommandDispatcher.class);
        stateRepository = new InMemoryUserStateRepository();
        scrapperClient = mock(ScrapperClient.class);
        BotMetrics botMetrics = new BotMetrics(new SimpleMeterRegistry());
        processor = new TelegramUpdateProcessor(bot, dispatcher, stateRepository, scrapperClient, botMetrics);

        when(bot.execute(any(SendMessage.class))).thenReturn(null);
    }

    @Test
    void unknownCommandInActiveDialog_keepsStateAndDoesNotDispatch() {
        long chatId = 101L;
        stateRepository.save(new UserState(chatId, ConversationState.WAITING_TRACK_URL, null, List.of()));
        when(dispatcher.isKnownCommand("/what")).thenReturn(false);

        processor.process(update(chatId, "/what"));

        UserState state = stateRepository.findByChatId(chatId).orElseThrow();
        assertThat(state.conversationState()).isEqualTo(ConversationState.WAITING_TRACK_URL);
        verify(dispatcher, never()).dispatch(any(), any(Long.class));
        verify(bot).execute(any(SendMessage.class));
    }

    @Test
    void waitingTrackTags_duplicateLink_returnsMessageAndResetsState() {
        long chatId = 102L;
        stateRepository.save(
                new UserState(chatId, ConversationState.WAITING_TRACK_TAGS, "https://github.com/u/r", List.of()));

        HttpClientErrorException conflict = HttpClientErrorException.create(
                HttpStatus.CONFLICT, "Conflict", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8);
        when(scrapperClient.addLink(eq(chatId), any(AddLinkRequest.class))).thenThrow(conflict);

        processor.process(update(chatId, "work, docs"));

        verify(scrapperClient)
                .addLink(chatId, new AddLinkRequest("https://github.com/u/r", List.of("work", "docs"), List.of()));
        UserState state = stateRepository.findByChatId(chatId).orElseThrow();
        assertThat(state.conversationState()).isEqualTo(ConversationState.IDLE);
        verify(bot).execute(any(SendMessage.class));
    }

    private Update update(long chatId, String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn(text);

        return update;
    }
}
