package backend.academy.linktracker.scrapper.repository.impl.orm;

import backend.academy.linktracker.scrapper.domain.TrackedLink;
import backend.academy.linktracker.scrapper.exception.DuplicateLinkException;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.entity.ChatLinkEntity;
import backend.academy.linktracker.scrapper.repository.impl.orm.entity.ChatLinkKey;
import backend.academy.linktracker.scrapper.repository.impl.orm.entity.LinkEntity;
import backend.academy.linktracker.scrapper.repository.impl.orm.repository.ChatLinkJpaRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.repository.LinkJpaRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.database-access-type", havingValue = "orm")
public class OrmLinkRepository implements LinkRepository {

    private final LinkJpaRepository linkJpaRepository;
    private final ChatLinkJpaRepository chatLinkJpaRepository;

    @Override
    @Transactional
    public TrackedLink addLink(long chatId, String url, List<String> tags, List<String> filters) {
        LinkEntity link = linkJpaRepository.findByUrl(url).orElseGet(() -> {
            LinkEntity entity = new LinkEntity();
            entity.setUrl(url);
            entity.setTags(join(tags));
            entity.setFilters(join(filters));
            entity.setLastUpdatedAt(Instant.EPOCH);
            return linkJpaRepository.save(entity);
        });

        if (chatLinkJpaRepository.existsByIdChatIdAndIdLinkId(chatId, link.getId())) {
            throw new DuplicateLinkException("already tracked");
        }
        chatLinkJpaRepository.save(new ChatLinkEntity(new ChatLinkKey(chatId, link.getId())));
        return toTracked(link, List.of(chatId));
    }

    @Override
    @Transactional
    public TrackedLink removeLink(long chatId, String url) {
        LinkEntity link = linkJpaRepository.findByUrl(url).orElse(null);
        if (link == null) {
            return null;
        }
        ChatLinkKey key = new ChatLinkKey(chatId, link.getId());
        if (!chatLinkJpaRepository.existsById(key)) {
            return null;
        }
        chatLinkJpaRepository.deleteById(key);
        List<Long> remainingChats = chatLinkJpaRepository.findAllByIdLinkId(link.getId()).stream()
                .map(rel -> rel.getId().getChatId())
                .toList();
        if (remainingChats.isEmpty()) {
            linkJpaRepository.deleteById(link.getId());
        }

        TrackedLink trackedLink = toTracked(link, new ArrayList<>(remainingChats));
        trackedLink.removeChat(chatId);
        return trackedLink;
    }

    @Override
    public List<TrackedLink> getLinksByChat(long chatId) {
        List<Long> linkIds = chatLinkJpaRepository.findAllByIdChatId(chatId).stream()
                .map(rel -> rel.getId().getLinkId())
                .toList();
        if (linkIds.isEmpty()) {
            return List.of();
        }
        Map<Long, LinkEntity> linksById = new HashMap<>();
        linkJpaRepository.findAllById(linkIds).forEach(link -> linksById.put(link.getId(), link));

        List<TrackedLink> result = new ArrayList<>();
        for (Long linkId : linkIds) {
            LinkEntity link = linksById.get(linkId);
            if (link != null) {
                result.add(toTracked(link, List.of(chatId)));
            }
        }
        return result;
    }

    @Override
    public List<TrackedLink> getAllTrackedLinks() {
        Map<Long, List<Long>> chatsByLink = new HashMap<>();
        for (var relation : chatLinkJpaRepository.findAll()) {
            chatsByLink
                    .computeIfAbsent(relation.getId().getLinkId(), it -> new ArrayList<>())
                    .add(relation.getId().getChatId());
        }

        List<TrackedLink> result = new ArrayList<>();
        for (LinkEntity link : linkJpaRepository.findAll()) {
            List<Long> chats = chatsByLink.getOrDefault(link.getId(), List.of());
            if (!chats.isEmpty()) {
                result.add(toTracked(link, chats));
            }
        }
        return result;
    }

    @Override
    public List<TrackedLink> getLinksAfter(long afterId, int limit) {
        List<LinkEntity> entities =
                linkJpaRepository.findByIdGreaterThanOrderByIdAsc(afterId, PageRequest.of(0, limit));
        List<Long> linkIds = entities.stream().map(LinkEntity::getId).toList();
        if (linkIds.isEmpty()) {
            return List.of();
        }
        Map<Long, List<Long>> chatsByLink = new HashMap<>();
        for (var relation : chatLinkJpaRepository.findAllByIdLinkIdIn(linkIds)) {
            chatsByLink
                    .computeIfAbsent(relation.getId().getLinkId(), it -> new ArrayList<>())
                    .add(relation.getId().getChatId());
        }
        List<TrackedLink> result = new ArrayList<>();
        for (LinkEntity link : entities) {
            List<Long> chats = chatsByLink.getOrDefault(link.getId(), List.of());
            if (!chats.isEmpty()) {
                result.add(toTracked(link, chats));
            }
        }
        return result;
    }

    @Override
    public void updateLastUpdatedAt(long linkId, Instant lastUpdatedAt) {
        linkJpaRepository.findById(linkId).ifPresent(link -> {
            link.setLastUpdatedAt(lastUpdatedAt);
            linkJpaRepository.save(link);
        });
    }

    private TrackedLink toTracked(LinkEntity entity, List<Long> chats) {
        TrackedLink trackedLink =
                new TrackedLink(entity.getId(), entity.getUrl(), split(entity.getTags()), split(entity.getFilters()));
        trackedLink.updateTimestamp(entity.getLastUpdatedAt());
        chats.forEach(trackedLink::addChat);
        return trackedLink;
    }

    private String join(List<String> values) {
        return values == null || values.isEmpty() ? "" : String.join(",", values);
    }

    private List<String> split(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        String[] items = value.split(",");
        List<String> out = new ArrayList<>(items.length);
        for (String item : items) {
            if (!item.isBlank()) {
                out.add(item.trim());
            }
        }
        return out;
    }
}
