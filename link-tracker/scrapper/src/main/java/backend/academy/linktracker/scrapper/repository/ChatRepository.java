package backend.academy.linktracker.scrapper.repository;

public interface ChatRepository {

    boolean registerChat(long chatId);

    boolean deleteChat(long chatId);

    boolean chatExists(long chatId);
}
