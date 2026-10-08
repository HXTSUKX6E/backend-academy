package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.configuration.AiAgentProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class HuggingFaceSummarizationService implements SummarizationService {

    private final RestClient restClient;
    private final AiAgentProperties properties;

    public HuggingFaceSummarizationService(RestClient.Builder restClientBuilder, AiAgentProperties properties) {
        this.restClient = restClientBuilder
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
        this.properties = properties;
    }

    @Override
    public String summarize(String text) {
        var hf = properties.summarization().huggingFace();

        if (hf.apiKey() == null || hf.apiKey().isBlank()) {
            log.atDebug().log("Hugging Face API key not configured, using truncation fallback");
            return truncate(text, properties.summarization().threshold());
        }

        try {
            var request = new HuggingFaceRequest(text);
            String url = hf.baseUrl() + "/models/" + hf.model();

            var response = restClient
                    .post()
                    .uri(url)
                    .header("Authorization", "Bearer " + hf.apiKey())
                    .body(request)
                    .retrieve()
                    .body(HuggingFaceResponse[].class);

            if (response != null && response.length > 0 && response[0].summaryText() != null) {
                return response[0].summaryText();
            }
            return truncate(text, properties.summarization().threshold());
        } catch (Exception e) {
            log.atWarn().setCause(e).log("Hugging Face API call failed, falling back to truncation");
            return truncate(text, properties.summarization().threshold());
        }
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    record HuggingFaceRequest(String inputs) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record HuggingFaceResponse(String summary_text) {
        String summaryText() {
            return summary_text;
        }
    }
}
