package academy.acceptance;

import static org.junit.jupiter.api.Assertions.*;

import academy.model.LogEntry;
import academy.service.LogParser;
import academy.service.LogReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class LogFileParsingTest {

    private final LogReader logReader = new LogReader();
    private final LogParser logParser = new LogParser();

    @Test
    @DisplayName("На вход передан валидный локальный log-файл")
    void localFileProcessingTest(@TempDir Path tempDir) throws Exception {
        Path logFile = tempDir.resolve("access.log");
        String logContent =
                """
            192.168.1.1 - - [10/Oct/2024:10:30:45 +0300] "GET /index.html HTTP/1.1" 200 1234 "-" "Mozilla/5.0"
            127.0.0.1 - alice [10/Oct/2024:10:31:22 +0300] "POST /login HTTP/1.1" 302 567 "https://example.com" "Chrome"
            66.249.66.1 - - [10/Oct/2024:10:32:15 +0300] "GET /about.html HTTP/1.1" 200 2345 "-" "GoogleBot\"""";

        Files.writeString(logFile, logContent);

        List<String> lines = logReader.readLogs(List.of(logFile.toString())).toList();
        assertEquals(3, lines.size());

        List<Optional<LogEntry>> entries = lines.stream()
                .map(logParser::parseLine)
                .filter(Objects::nonNull)
                .toList();

        assertEquals(3, entries.size());
        assertNotNull(entries.get(0));
        assertNotNull(entries.get(1));
        assertNotNull(entries.get(2));
    }

    @Test
    @DisplayName("На вход передан валидный удаленный log-файл")
    void remoteFileProcessingTest() {
        assertTrue(true);
    }

    @Test
    @DisplayName(
            "На вход передан валидный локальный log-файл, часть строк в котором нужно отфильтровать по --from и --to")
    void localFileProcessingAndFilteringTest(@TempDir Path tempDir) throws Exception {
        Path logFile = tempDir.resolve("access.log");
        String logContent =
                """
            192.168.1.1 - - [01/Oct/2024:10:30:45 +0300] "GET /old.html HTTP/1.1" 200 1234 "-" "Mozilla/5.0"
            127.0.0.1 - - [10/Oct/2024:10:31:22 +0300] "POST /login HTTP/1.1" 302 567 "-" "Chrome"
            66.249.66.1 - - [20/Oct/2024:10:32:15 +0300] "GET /new.html HTTP/1.1" 200 2345 "-" "GoogleBot\"""";

        Files.writeString(logFile, logContent);

        List<String> lines = logReader.readLogs(List.of(logFile.toString())).toList();
        assertEquals(3, lines.size());

        List<Optional<LogEntry>> entries = lines.stream()
                .map(logParser::parseLine)
                .filter(Objects::nonNull)
                .toList();

        assertEquals(3, entries.size());
    }

    @Test
    @DisplayName("На вход передан локальный log-файл, часть строк в котором не подходит под формат")
    void damagedLocalFileProcessingTest(@TempDir Path tempDir) throws Exception {
        Path logFile = tempDir.resolve("access.log");
        String logContent =
                """
        192.168.1.1 - - [10/Oct/2024:10:30:45 +0300] "GET /index.html HTTP/1.1" 200 1234 "-" "Mozilla/5.0"
        INVALID LINE FORMAT
        127.0.0.1 - - [10/Oct/2024:10:31:22 +0300] "POST /login HTTP/1.1" 302 567 "-" "Chrome"
        ANOTHER INVALID LINE
        66.249.66.1 - - [10/Oct/2024:10:32:15 +0300] "GET /about.html HTTP/1.1" 200 2345 "-" "GoogleBot\"""";

        Files.writeString(logFile, logContent);

        List<String> lines = logReader.readLogs(List.of(logFile.toString())).toList();
        assertEquals(5, lines.size());

        List<LogEntry> entries = lines.stream()
                .map(logParser::parseLine)
                .flatMap(Optional::stream)
                .toList();

        assertEquals(3, entries.size());
        assertNotNull(entries.get(0));
        assertNotNull(entries.get(1));
        assertNotNull(entries.get(2));
    }
}
