package backend.academy.linktracker.bot.repository;

import backend.academy.linktracker.bot.domain.UserState;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryUserStateRepository implements UserStateRepository {

    private final ConcurrentHashMap<Long, UserState> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<UserState> findByChatId(long chatId) {
        return Optional.ofNullable(storage.get(chatId));
    }

    @Override
    public void save(UserState state) {
        storage.put(state.chatId(), state);
    }
}
