package backend.academy.linktracker.scrapper.repository.impl.orm;

import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.entity.ChatEntity;
import backend.academy.linktracker.scrapper.repository.impl.orm.repository.ChatJpaRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.repository.ChatLinkJpaRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.repository.LinkJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.database-access-type", havingValue = "orm")
public class OrmChatRepository implements ChatRepository {

    private final ChatJpaRepository chatJpaRepository;
    private final ChatLinkJpaRepository chatLinkJpaRepository;
    private final LinkJpaRepository linkJpaRepository;

    @Override
    public boolean registerChat(long chatId) {
        if (chatJpaRepository.existsById(chatId)) {
            return false;
        }
        chatJpaRepository.save(new ChatEntity(chatId));
        return true;
    }

    @Override
    @Transactional
    public boolean deleteChat(long chatId) {
        if (!chatJpaRepository.existsById(chatId)) {
            return false;
        }
        chatLinkJpaRepository.deleteAllByIdChatId(chatId);
        chatJpaRepository.deleteById(chatId);
        deleteOrphanLinks();
        return true;
    }

    @Override
    public boolean chatExists(long chatId) {
        return chatJpaRepository.existsById(chatId);
    }

    private void deleteOrphanLinks() {
        for (var link : linkJpaRepository.findAll()) {
            if (chatLinkJpaRepository.findAllByIdLinkId(link.getId()).isEmpty()) {
                linkJpaRepository.deleteById(link.getId());
            }
        }
    }
}
