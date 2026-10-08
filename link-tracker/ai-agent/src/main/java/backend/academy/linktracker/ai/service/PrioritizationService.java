package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.configuration.AiAgentProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrioritizationService {

    private final AiAgentProperties properties;

    public Priority prioritize(String description) {
        String lower = description.toLowerCase();
        var p = properties.prioritization();
        if (p.highKeywords().stream().anyMatch(kw -> lower.contains(kw.toLowerCase()))) {
            return Priority.HIGH;
        }
        if (p.lowKeywords().stream().anyMatch(kw -> lower.contains(kw.toLowerCase()))) {
            return Priority.LOW;
        }
        return Priority.MEDIUM;
    }
}
