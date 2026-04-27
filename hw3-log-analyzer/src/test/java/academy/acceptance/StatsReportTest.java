package academy.acceptance;

import static org.junit.jupiter.api.Assertions.*;

import academy.cli.OutputFormat;
import academy.model.Statistics;
import academy.service.report.CompositeReportGenerator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class StatsReportTest {

    private final CompositeReportGenerator reportGenerator = new CompositeReportGenerator();

    @Test
    @DisplayName("Сохранение статистики в формате JSON")
    void jsonTest(@TempDir Path tempDir) throws Exception {
        Statistics statistics = createTestStatistics();
        Path outputPath = tempDir.resolve("report.json");

        reportGenerator.generateReport(statistics, OutputFormat.JSON, outputPath);

        assertTrue(Files.exists(outputPath));
        assertTrue(Files.size(outputPath) > 0);
    }

    @Test
    @DisplayName("Сохранение статистики в формате MARKDOWN")
    void markdownTest(@TempDir Path tempDir) throws Exception {
        Statistics statistics = createTestStatistics();
        Path outputPath = tempDir.resolve("report.md");

        reportGenerator.generateReport(statistics, OutputFormat.MARKDOWN, outputPath);

        assertTrue(Files.exists(outputPath));
        assertTrue(Files.size(outputPath) > 0);
    }

    @Test
    @DisplayName("Сохранение статистики в формате ADOC")
    void adocTest(@TempDir Path tempDir) throws Exception {
        Statistics statistics = createTestStatistics();
        Path outputPath = tempDir.resolve("report.adoc");

        reportGenerator.generateReport(statistics, OutputFormat.ADOC, outputPath);

        assertTrue(Files.exists(outputPath));
        assertTrue(Files.size(outputPath) > 0);
    }

    @Test
    @DisplayName("Создание отчета с правильным расширением файла")
    void fileExtensionTest(@TempDir Path tempDir) throws Exception {
        Statistics statistics = createTestStatistics();

        Path jsonPath = tempDir.resolve("report.json");
        Path mdPath = tempDir.resolve("report.md");
        Path adocPath = tempDir.resolve("report.adoc");

        reportGenerator.generateReport(statistics, OutputFormat.JSON, jsonPath);
        reportGenerator.generateReport(statistics, OutputFormat.MARKDOWN, mdPath);
        reportGenerator.generateReport(statistics, OutputFormat.ADOC, adocPath);

        assertTrue(Files.exists(jsonPath));
        assertTrue(Files.exists(mdPath));
        assertTrue(Files.exists(adocPath));
    }

    @Test
    @DisplayName("Генерация отчета для пустой статистики")
    void emptyStatisticsTest(@TempDir Path tempDir) throws Exception {
        Statistics emptyStatistics = createEmptyStatistics();
        Path outputPath = tempDir.resolve("empty_report.md");

        reportGenerator.generateReport(emptyStatistics, OutputFormat.MARKDOWN, outputPath);

        assertTrue(Files.exists(outputPath));
        assertTrue(Files.size(outputPath) > 0);
    }

    @Test
    @DisplayName("Попытка генерации отчета в неподдерживаемом формате")
    void unsupportedFormatTest(@TempDir Path tempDir) {
        Statistics statistics = createTestStatistics();
        Path outputPath = tempDir.resolve("report.txt");

        assertDoesNotThrow(() -> {
            reportGenerator.generateReport(statistics, OutputFormat.JSON, outputPath);
        });
    }

    @Test
    @DisplayName("Проверка содержимого JSON отчета")
    void jsonContentTest(@TempDir Path tempDir) throws Exception {
        Statistics statistics = createTestStatistics();
        Path outputPath = tempDir.resolve("report.json");

        reportGenerator.generateReport(statistics, OutputFormat.JSON, outputPath);

        String content = Files.readString(outputPath);
        assertTrue(content.contains("\"files\""));
        assertTrue(content.contains("\"totalRequestsCount\""));
        assertTrue(content.contains("\"resources\""));
        assertTrue(content.contains("\"responseCodes\""));
    }

    @Test
    @DisplayName("Проверка содержимого Markdown отчета")
    void markdownContentTest(@TempDir Path tempDir) throws Exception {
        Statistics statistics = createTestStatistics();
        Path outputPath = tempDir.resolve("report.md");

        reportGenerator.generateReport(statistics, OutputFormat.MARKDOWN, outputPath);

        String content = Files.readString(outputPath);
        assertTrue(content.contains("#### Информация"));
        assertTrue(content.contains("| Метрика | Значение |"));
        assertTrue(content.contains("#### Запрашиваемые ресурсы"));
        assertTrue(content.contains("#### Коды ответа"));
    }

    private Statistics createTestStatistics() {
        return new Statistics(
                List.of("access.log"),
                100,
                new Statistics.ResponseSize(1024.5, 2048.0, 1536.2),
                List.of(
                        new Statistics.ResourceStat("/index.html", 50),
                        new Statistics.ResourceStat("/about.html", 30),
                        new Statistics.ResourceStat("/contact.html", 20)),
                List.of(
                        new Statistics.ResponseCodeStat(200, 80),
                        new Statistics.ResponseCodeStat(404, 15),
                        new Statistics.ResponseCodeStat(500, 5)),
                List.of(new Statistics.DateStat("2024-10-10", "THURSDAY", 100, 100.0)),
                List.of("HTTP/1.1"));
    }

    private Statistics createEmptyStatistics() {
        return new Statistics(
                List.of(), 0, new Statistics.ResponseSize(0, 0, 0), List.of(), List.of(), List.of(), List.of());
    }
}
