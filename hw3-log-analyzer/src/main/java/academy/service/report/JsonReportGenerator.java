package academy.service.report;

import academy.model.Statistics;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;

public class JsonReportGenerator extends BaseReportGenerator {
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    protected String generateContent(Statistics statistics) {
        try {
            return objectMapper.writeValueAsString(statistics);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize statistics to JSON", e);
        }
    }
}
