package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.LinkUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BotClient {

    private final BotNotifierClient delegate;

    public void sendUpdate(LinkUpdateRequest request) {
        delegate.sendUpdate(request);
    }
}
