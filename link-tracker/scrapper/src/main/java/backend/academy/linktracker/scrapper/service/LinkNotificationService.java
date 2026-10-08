package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.TrackedLink;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import java.util.List;

public interface LinkNotificationService {

    void sendUpdate(TrackedLink link, LinkUpdate update);

    void sendFailureReport(String url, List<Long> chatIds);
}
