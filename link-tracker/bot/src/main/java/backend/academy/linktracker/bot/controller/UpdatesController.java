package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.grpc.UpdateNotifier;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/updates")
@RequiredArgsConstructor
public class UpdatesController {

    private final UpdateNotifier notifier;

    @PostMapping
    public ResponseEntity<Void> sendUpdate(@Valid @RequestBody LinkUpdate update) {
        notifier.notifyUpdate(update);
        log.atInfo()
                .addKeyValue("link", update.url())
                .addKeyValue("chatCount", update.tgChatIds().size())
                .log("REST update sent");
        return ResponseEntity.ok().build();
    }
}
