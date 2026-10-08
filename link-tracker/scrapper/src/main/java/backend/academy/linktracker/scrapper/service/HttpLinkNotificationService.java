package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.client.BotNotifierClient;
import backend.academy.linktracker.scrapper.domain.TrackedLink;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.LinkUpdateRequest;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HttpLinkNotificationService implements LinkNotificationService {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of("UTC"));
    private static final String UPDATE_FORMAT = "%s%n%s%nАвтор: %s%nДата: %s%n%s";

    private final BotNotifierClient botNotifierClient;

    @Override
    public void sendUpdate(TrackedLink link, LinkUpdate update) {
        String description = buildDescription(update);
        botNotifierClient.sendUpdate(new LinkUpdateRequest(
                link.getId(), link.getUrl(), description, new ArrayList<>(link.getChatIds()), update.author()));
        log.atInfo()
                .addKeyValue("url", link.getUrl())
                .addKeyValue("chatCount", link.getChatIds().size())
                .log("Update notification sent via HTTP");
    }

    @Override
    public void sendFailureReport(String url, List<Long> chatIds) {
        if (chatIds.isEmpty()) {
            return;
        }
        String message = "Не удалось проверить обновление по ссылке: " + url;
        botNotifierClient.sendUpdate(new LinkUpdateRequest(0L, url, message, new ArrayList<>(chatIds)));
        log.atWarn()
                .addKeyValue("url", url)
                .addKeyValue("chatCount", chatIds.size())
                .log("Failure report sent");
    }

    private String buildDescription(LinkUpdate update) {
        String typeLabel = update.type() != null ? update.type().getLabel() : "";
        return String.format(
                UPDATE_FORMAT,
                typeLabel,
                update.title() != null ? update.title() : "",
                update.author() != null ? update.author() : "",
                DATE_FMT.format(update.createdAt()),
                update.bodyPreview() != null ? update.bodyPreview() : "");
    }
}
