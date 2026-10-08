package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.TrackedLink;
import backend.academy.linktracker.scrapper.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.dto.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.exception.DuplicateLinkException;
import backend.academy.linktracker.scrapper.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;

    public void registerChat(long chatId) {
        chatRepository.registerChat(chatId);
    }

    @CacheEvict(value = "links", key = "#chatId")
    public boolean deleteChat(long chatId) {
        return chatRepository.deleteChat(chatId);
    }

    @CacheEvict(value = "links", key = "#chatId")
    public LinkResponse addLink(long chatId, AddLinkRequest request) {
        if (!chatRepository.chatExists(chatId)) {
            throw new ChatNotFoundException();
        }
        try {
            TrackedLink link = linkRepository.addLink(chatId, request.link(), request.tags(), request.filters());
            return toResponse(link);
        } catch (IllegalStateException ex) {
            throw new DuplicateLinkException();
        }
    }

    @CacheEvict(value = "links", key = "#chatId")
    public LinkResponse removeLink(long chatId, RemoveLinkRequest request) {
        if (!chatRepository.chatExists(chatId)) {
            throw new ChatNotFoundException();
        }
        TrackedLink link = linkRepository.removeLink(chatId, request.link());
        if (link == null) {
            throw new LinkNotFoundException();
        }
        return toResponse(link);
    }

    @Cacheable(value = "links", key = "#chatId")
    public ListLinksResponse listLinks(long chatId) {
        if (!chatRepository.chatExists(chatId)) {
            throw new ChatNotFoundException();
        }
        List<LinkResponse> links = linkRepository.getLinksByChat(chatId).stream()
                .map(this::toResponse)
                .toList();
        return new ListLinksResponse(links, links.size());
    }

    private LinkResponse toResponse(TrackedLink link) {
        return new LinkResponse(link.getId(), link.getUrl(), link.getTags(), link.getFilters());
    }
}
