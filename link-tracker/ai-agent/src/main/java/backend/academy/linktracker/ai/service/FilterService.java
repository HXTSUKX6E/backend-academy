package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.configuration.AiAgentProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilterService {

    private final AiAgentProperties properties;

    public boolean shouldProcess(String description, String author) {
        var filtering = properties.filtering();

        if (description == null || description.length() < filtering.minLength()) {
            log.atDebug()
                    .addKeyValue("descriptionLength", description == null ? 0 : description.length())
                    .addKeyValue("minLength", filtering.minLength())
                    .log("Update filtered out: text too short");
            return false;
        }

        if (author != null && containsExcludedAuthor(author, filtering.excludedAuthors())) {
            log.atDebug().addKeyValue("author", author).log("Update filtered out: excluded author");
            return false;
        }

        if (containsStopWord(description, filtering.stopWords())) {
            log.atDebug().log("Update filtered out: contains stop word");
            return false;
        }

        return true;
    }

    private boolean containsExcludedAuthor(String author, List<String> excludedAuthors) {
        return excludedAuthors.stream().anyMatch(excluded -> excluded.equalsIgnoreCase(author));
    }

    private boolean containsStopWord(String text, List<String> stopWords) {
        String lowerText = text.toLowerCase();
        return stopWords.stream().anyMatch(word -> lowerText.contains(word.toLowerCase()));
    }
}
