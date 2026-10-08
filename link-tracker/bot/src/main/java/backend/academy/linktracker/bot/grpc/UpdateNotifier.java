package backend.academy.linktracker.bot.grpc;

import backend.academy.linktracker.bot.dto.LinkUpdate;

public interface UpdateNotifier {
    void notifyUpdate(LinkUpdate update);
}
