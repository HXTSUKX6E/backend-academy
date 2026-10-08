package backend.academy.linktracker.bot.telegram.command;

public interface BotCommandHandler {
    String command();

    String description();

    String handle(long chatId, String text);
}
