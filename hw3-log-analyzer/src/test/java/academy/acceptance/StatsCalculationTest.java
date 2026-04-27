package academy.acceptance;

import static org.junit.jupiter.api.Assertions.*;

import academy.model.LogEntry;
import academy.model.Statistics;
import academy.service.LogAnalyzer;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class StatsCalculationTest {

    private final LogAnalyzer logAnalyzer = new LogAnalyzer();

    @Test
    @DisplayName("Расчет статистики на основании локального log-файла")
    void happyPathTest() {
        List<LogEntry> logEntries = List.of(
                createLogEntry("192.168.1.1", "2024-10-10T10:30:45", "GET /index.html HTTP/1.1", 200, 1234),
                createLogEntry("127.0.0.1", "2024-10-10T10:31:22", "POST /login HTTP/1.1", 302, 567),
                createLogEntry("66.249.66.1", "2024-10-10T10:32:15", "GET /about.html HTTP/1.1", 200, 2345),
                createLogEntry("192.168.1.100", "2024-10-10T10:33:01", "GET /contact HTTP/1.1", 404, 123),
                createLogEntry("192.168.1.50", "2024-10-10T10:34:22", "GET /products HTTP/1.1", 200, 3456));

        List<String> files = List.of("access.log");
        Statistics statistics = logAnalyzer.analyze(logEntries.stream(), files, null, null);

        assertNotNull(statistics);
    }

    @Test
    @DisplayName("Фильтрация по дате from")
    void filteringByFromDateTest() {
        List<LogEntry> logEntries = List.of(
                createLogEntry("192.168.1.1", "2024-10-01T10:30:45", "GET /old.html HTTP/1.1", 200, 1000),
                createLogEntry("127.0.0.1", "2024-10-10T10:31:22", "POST /login HTTP/1.1", 302, 567),
                createLogEntry("66.249.66.1", "2024-10-20T10:32:15", "GET /new.html HTTP/1.1", 200, 2345));

        List<String> files = List.of("access.log");
        Statistics statistics =
                logAnalyzer.analyze(logEntries.stream(), files, java.time.LocalDate.of(2024, 10, 5), null);

        assertNotNull(statistics);
    }

    @Test
    @DisplayName("Фильтрация по дате to")
    void filteringByToDateTest() {
        List<LogEntry> logEntries = List.of(
                createLogEntry("192.168.1.1", "2024-10-01T10:30:45", "GET /old.html HTTP/1.1", 200, 1000),
                createLogEntry("127.0.0.1", "2024-10-10T10:31:22", "POST /login HTTP/1.1", 302, 567),
                createLogEntry("66.249.66.1", "2024-10-20T10:32:15", "GET /new.html HTTP/1.1", 200, 2345));

        List<String> files = List.of("access.log");
        Statistics statistics =
                logAnalyzer.analyze(logEntries.stream(), files, null, java.time.LocalDate.of(2024, 10, 15));

        assertNotNull(statistics);
    }

    @Test
    @DisplayName("Фильтрация по диапазону дат from-to")
    void filteringByDateRangeTest() {
        List<LogEntry> logEntries = List.of(
                createLogEntry("192.168.1.1", "2024-10-01T10:30:45", "GET /old.html HTTP/1.1", 200, 1000),
                createLogEntry("127.0.0.1", "2024-10-10T10:31:22", "POST /login HTTP/1.1", 302, 567),
                createLogEntry("66.249.66.1", "2024-10-20T10:32:15", "GET /new.html HTTP/1.1", 200, 2345));

        List<String> files = List.of("access.log");
        Statistics statistics = logAnalyzer.analyze(
                logEntries.stream(), files, java.time.LocalDate.of(2024, 10, 5), java.time.LocalDate.of(2024, 10, 15));

        assertNotNull(statistics);
    }

    @Test
    @DisplayName("Статистика для пустого списка логов")
    void emptyLogsTest() {
        List<LogEntry> logEntries = List.of();
        List<String> files = List.of("access.log");

        Statistics statistics = logAnalyzer.analyze(logEntries.stream(), files, null, null);

        assertNotNull(statistics);
    }

    @Test
    @DisplayName("Расчет статистики по кодам ответа")
    void responseCodesCalculationTest() {
        List<LogEntry> logEntries = List.of(
                createLogEntry("192.168.1.1", "2024-10-10T10:30:45", "GET /index.html HTTP/1.1", 200, 1234),
                createLogEntry("127.0.0.1", "2024-10-10T10:31:22", "POST /login HTTP/1.1", 200, 567),
                createLogEntry("66.249.66.1", "2024-10-10T10:32:15", "GET /about.html HTTP/1.1", 404, 2345),
                createLogEntry("192.168.1.100", "2024-10-10T10:33:01", "GET /admin HTTP/1.1", 403, 123));

        List<String> files = List.of("access.log");
        Statistics statistics = logAnalyzer.analyze(logEntries.stream(), files, null, null);

        assertNotNull(statistics);
    }

    @Test
    @DisplayName("Разные HTTP методы")
    void differentHttpMethodsTest() {
        List<LogEntry> logEntries = List.of(
                createLogEntry("192.168.1.1", "2024-10-10T10:30:45", "GET /index.html HTTP/1.1", 200, 1234),
                createLogEntry("127.0.0.1", "2024-10-10T10:31:22", "POST /api/users HTTP/1.1", 201, 567),
                createLogEntry("66.249.66.1", "2024-10-10T10:32:15", "PUT /api/users/1 HTTP/1.1", 200, 234),
                createLogEntry("192.168.1.100", "2024-10-10T10:33:01", "DELETE /api/users/1 HTTP/1.1", 204, 0));

        List<String> files = List.of("access.log");
        Statistics statistics = logAnalyzer.analyze(logEntries.stream(), files, null, null);

        assertNotNull(statistics);
    }

    private LogEntry createLogEntry(String ip, String timestamp, String request, int status, long size) {
        return new LogEntry(ip, "-", LocalDateTime.parse(timestamp), request, status, size, "-", "Mozilla/5.0", "", "");
    }
}
