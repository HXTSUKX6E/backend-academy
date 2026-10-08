package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.kafka.ProcessedUpdateProducer;
import com.example.notification.ProcessedUpdateEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupingService {

    private final ProcessedUpdateProducer producer;

    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<PrioritizedUpdate>> buffer = new ConcurrentHashMap<>();

    public void buffer(PrioritizedUpdate update) {
        for (Long chatId : update.tgChatIds()) {
            buffer.computeIfAbsent(chatId, k -> new CopyOnWriteArrayList<>()).add(update);
        }
    }

    @Scheduled(fixedDelayString = "${ai-agent.grouping.window-ms}")
    public void flush() {
        groupAndFlush().forEach(producer::publish);
    }

    public List<ProcessedUpdateEvent> groupAndFlush() {
        Map<Long, List<PrioritizedUpdate>> snapshot = new HashMap<>();
        for (Long chatId : new ArrayList<>(buffer.keySet())) {
            List<PrioritizedUpdate> updates = buffer.remove(chatId);
            if (updates != null && !updates.isEmpty()) {
                snapshot.put(chatId, new ArrayList<>(updates));
            }
        }

        List<ProcessedUpdateEvent> result = new ArrayList<>();
        for (Map.Entry<Long, List<PrioritizedUpdate>> entry : snapshot.entrySet()) {
            result.add(buildEvent(entry.getKey(), entry.getValue()));
        }

        log.atInfo().addKeyValue("groups", result.size()).log("Flushed grouping buffer");
        return result;
    }

    private ProcessedUpdateEvent buildEvent(Long chatId, List<PrioritizedUpdate> updates) {
        if (updates.size() == 1) {
            PrioritizedUpdate u = updates.get(0);
            return ProcessedUpdateEvent.newBuilder()
                    .setId(u.id())
                    .setUrl(u.url())
                    .setDescription(u.description())
                    .setTgChatIds(new ArrayList<>(u.tgChatIds()))
                    .setPriority(u.priority().name())
                    .build();
        }

        Priority maxPriority = Priority.LOW;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < updates.size(); i++) {
            PrioritizedUpdate u = updates.get(i);
            sb.append(i + 1).append(". ").append(u.description());
            if (i < updates.size() - 1) {
                sb.append("\n");
            }
            maxPriority = Priority.max(maxPriority, u.priority());
        }

        PrioritizedUpdate first = updates.get(0);
        return ProcessedUpdateEvent.newBuilder()
                .setId(first.id())
                .setUrl(first.url())
                .setDescription(sb.toString())
                .setTgChatIds(List.of(chatId))
                .setPriority(maxPriority.name())
                .build();
    }
}
