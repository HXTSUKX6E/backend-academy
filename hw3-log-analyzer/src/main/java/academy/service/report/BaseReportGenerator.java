package academy.service.report;

import academy.model.Statistics;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class BaseReportGenerator implements ReportGenerator {

    @Override
    public final void generateReport(Statistics statistics, Path outputPath) {
        try {
            String content = generateContent(statistics);
            Files.writeString(outputPath, content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write report to: " + outputPath, e);
        }
    }

    protected abstract String generateContent(Statistics statistics);

    protected final String formatNumber(int number) {
        return String.format("%,d", number).replace(",", "_");
    }

    protected final String getHttpStatusName(int code) {
        return switch (code) {
            case 200 -> "OK";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            case 304 -> "Not Modified";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            default -> "Unknown";
        };
    }
}
