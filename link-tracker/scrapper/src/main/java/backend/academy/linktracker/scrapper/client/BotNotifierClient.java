package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.LinkUpdateRequest;

public interface BotNotifierClient {
    void sendUpdate(LinkUpdateRequest request);
}
