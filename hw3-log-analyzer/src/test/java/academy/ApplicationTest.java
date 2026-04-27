package academy;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ApplicationTest {

    @Test
    @DisplayName("Успешный сценарий с корректными логами")
    void happyPathWithValidLogs(@TempDir Path tempDir) throws Exception {
        Path logFile = tempDir.resolve("test.log");
        Files.writeString(
                logFile,
                """
        192.168.1.1 - - [25/Dec/2023:10:15:32 +0000] "GET /index.html HTTP/1.1" 200 1024 "-" "Mozilla/5.0"
        192.168.1.2 - - [25/Dec/2023:10:16:45 +0000] "POST /api/login HTTP/1.1" 201 512 "-" "Mozilla/5.0"
        """);

        Path outputFile = tempDir.resolve("report.json");

        String[] args = {
            "--path", logFile.toString(),
            "--format", "json",
            "--output", outputFile.toString()
        };

        // Используйте execute вместо main
        int exitCode = Application.execute(args);

        assertEquals(0, exitCode, "Код возврата должен быть 0");
        assertTrue(Files.exists(outputFile), "Отчет должен быть создан");
        assertTrue(Files.size(outputFile) > 0, "Отчет не должен быть пустым");
    }

    @Test
    @DisplayName("Обработка пустого лог-файла")
    void shouldHandleEmptyLogFile(@TempDir Path tempDir) throws Exception {
        Path logFile = tempDir.resolve("empty.log");
        Files.createFile(logFile);

        Path outputFile = tempDir.resolve("report.json");
        String[] args = {
            "--path", logFile.toString(),
            "--format", "json",
            "--output", outputFile.toString()
        };

        int exitCode = Application.execute(args);

        assertEquals(0, exitCode, "Код возврата должен быть 0");
        assertTrue(Files.exists(outputFile));
    }

    @Test
    @DisplayName("Проверка разных форматов вывода")
    void testDifferentOutputFormats(@TempDir Path tempDir) throws Exception {
        String[][] formatsAndExtensions = {
            {"json", "json"},
            {"markdown", "md"},
            {"adoc", "adoc"}
        };

        for (String[] formatInfo : formatsAndExtensions) {
            String format = formatInfo[0];
            String extension = formatInfo[1];

            Path logFile = tempDir.resolve("test_" + format + ".log");
            Files.writeString(logFile, createValidLogEntry());

            Path outputFile = tempDir.resolve("report." + extension);
            String[] args = {
                "--path", logFile.toString(),
                "--format", format,
                "--output", outputFile.toString()
            };

            int exitCode = Application.execute(args);

            assertEquals(0, exitCode, "Формат " + format + " должен возвращать код 0");
            assertTrue(Files.exists(outputFile), "Файл отчета для формата " + format + " должен быть создан");
        }
    }

    @Test
    @DisplayName("Проверка работы с датами")
    void testWithDateFilters(@TempDir Path tempDir) throws Exception {
        Path logFile = tempDir.resolve("dated.log");
        Files.writeString(
                logFile,
                """
        192.168.1.1 - - [25/Dec/2023:10:15:32 +0000] "GET /index.html HTTP/1.1" 200 1024 "-" "Mozilla/5.0"
        192.168.1.2 - - [26/Dec/2023:10:16:45 +0000] "POST /api/login HTTP/1.1" 201 512 "-" "Mozilla/5.0"
        """);

        Path outputFile = tempDir.resolve("report_with_dates.json");
        String[] args = {
            "--path", logFile.toString(),
            "--format", "json",
            "--output", outputFile.toString(),
            "--from", "2023-12-25",
            "--to", "2023-12-26"
        };

        int exitCode = Application.execute(args);

        assertEquals(0, exitCode, "Код возврата должен быть 0");
        assertTrue(Files.exists(outputFile));
    }

    @Test
    @DisplayName("Тест с невалидными аргументами")
    void testWithInvalidArguments(@TempDir Path tempDir) throws Exception {
        String[] args = {
            "--format", "json", "--output", tempDir.resolve("report.json").toString()
            // Пропущен обязательный параметр --path
        };

        int exitCode = Application.execute(args);

        assertEquals(2, exitCode, "Код возврата для невалидных аргументов должен быть 2");
    }

    private String createValidLogEntry() {
        return "192.168.1.1 - - [25/Dec/2023:10:15:32 +0000] \"GET /index.html HTTP/1.1\" 200 1024 \"-\" \"Mozilla/5.0\"";
    }
}
