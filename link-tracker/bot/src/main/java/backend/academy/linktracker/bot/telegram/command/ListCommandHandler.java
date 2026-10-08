package backend.academy.linktracker.bot.telegram.command;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.LinkResponse;
import io.micrometer.common.util.StringUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListCommandHandler implements BotCommandHandler {

    private final ScrapperClient scrapperClient;

    @Override
    public String command() {
        return "/list";
    }

    @Override
    public String description() {
        return "список отслеживаемых ссылок";
    }

    @Override
    public String handle(long chatId, String text) {
        String[] parts = text.trim().split("\\s+", 2);
        String tag = parts.length > 1 ? parts[1].trim() : null;

        List<LinkResponse> links = scrapperClient.listLinks(chatId).links();
        if (!StringUtils.isBlank(tag)) {
            links = links.stream()
                    .filter(link -> link.tags() != null && link.tags().contains(tag))
                    .toList();
        }

        if (links.isEmpty()) {
            return "Список отслеживаемых ссылок пуст";
        }

        StringBuilder sb = new StringBuilder("Отслеживаемые ссылки:\n");
        for (LinkResponse link : links) {
            sb.append("- ").append(link.url());
            if (link.tags() != null && !link.tags().isEmpty()) {
                sb.append(" [").append(String.join(", ", link.tags())).append("]");
            }
            sb.append('\n');
        }
        return sb.toString().trim();
    }
}
