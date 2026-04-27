package academy.service.report;

import academy.cli.OutputFormat;
import academy.model.Statistics;
import java.nio.file.Path;
import java.util.Map;

public class CompositeReportGenerator {
    private final Map<OutputFormat, ReportGenerator> generators;

    public CompositeReportGenerator() {
        this.generators = Map.of(
                OutputFormat.JSON, new JsonReportGenerator(),
                OutputFormat.MARKDOWN, new MarkdownReportGenerator(),
                OutputFormat.ADOC, new AdocReportGenerator());
    }

    public void generateReport(Statistics statistics, OutputFormat format, Path outputPath) {
        ReportGenerator generator = generators.get(format);
        if (generator == null) {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }
        generator.generateReport(statistics, outputPath);
    }
}
