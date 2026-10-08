package backend.academy.linktracker.scrapper.repository.impl.orm.repository;

import backend.academy.linktracker.scrapper.repository.impl.orm.entity.ChatLinkEntity;
import backend.academy.linktracker.scrapper.repository.impl.orm.entity.ChatLinkKey;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatLinkJpaRepository extends JpaRepository<ChatLinkEntity, ChatLinkKey> {

    List<ChatLinkEntity> findAllByIdChatId(Long chatId);

    List<ChatLinkEntity> findAllByIdLinkId(Long linkId);

    List<ChatLinkEntity> findAllByIdLinkIdIn(List<Long> linkIds);

    boolean existsByIdChatIdAndIdLinkId(Long chatId, Long linkId);

    void deleteAllByIdChatId(Long chatId);
}
