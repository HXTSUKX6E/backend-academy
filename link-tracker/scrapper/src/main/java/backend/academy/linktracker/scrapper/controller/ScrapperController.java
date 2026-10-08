package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.dto.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.service.LinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ScrapperController {

    private final LinkService linkService;

    @PostMapping("/tg-chat/{id}")
    public ResponseEntity<Void> registerChat(@PathVariable long id) {
        linkService.registerChat(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tg-chat/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable long id) {
        boolean deleted = linkService.deleteChat(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/links")
    public ListLinksResponse getLinks(@RequestHeader("Tg-Chat-Id") long chatId) {
        return linkService.listLinks(chatId);
    }

    @PostMapping("/links")
    public LinkResponse addLink(@RequestHeader("Tg-Chat-Id") long chatId, @Valid @RequestBody AddLinkRequest request) {
        return linkService.addLink(chatId, request);
    }

    @DeleteMapping("/links")
    public LinkResponse removeLink(
            @RequestHeader("Tg-Chat-Id") long chatId, @Valid @RequestBody RemoveLinkRequest request) {
        return linkService.removeLink(chatId, request);
    }
}
