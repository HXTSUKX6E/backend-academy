package academy.service.report;

import academy.model.Statistics;
import java.nio.file.Path;

public interface ReportGenerator {
    void generateReport(Statistics statistics, Path outputPath);
}
