package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import java.time.Instant;
import java.util.List;

public interface LinkUpdateChecker {

    boolean supports(String url);

    List<LinkUpdate> check(String url, Instant since);
}
