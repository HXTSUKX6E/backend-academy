package backend.academy.linktracker.bot.repository;

import backend.academy.linktracker.bot.domain.UserState;
import java.util.Optional;

public interface UserStateRepository {
    Optional<UserState> findByChatId(long chatId);

    void save(UserState state);
}
