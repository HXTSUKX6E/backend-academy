package academy.service;

import academy.model.LogEntry;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogParser {
    private static final Logger logger = LogManager.getLogger(LogParser.class);

    private static final String LOG_PATTERN =
            "^(\\S+) - (\\S+) \\[(.+?)\\] \"(.+?)\" (\\d{3}) (\\d+) \"(.+?)\" \"(.+?)\"$";
    private static final Pattern PATTERN = Pattern.compile(LOG_PATTERN);

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("d/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);

    public Optional<LogEntry> parseLine(String line) {
        if (line == null || line.isBlank()) {
            logger.debug("Empty or null line provided");
            return Optional.empty();
        }

        Matcher matcher = PATTERN.matcher(line);
        if (!matcher.matches()) {
            logger.warn("Line doesn't match log format: {}", line);
            return Optional.empty();
        }

        try {
            LogEntry logEntry = new LogEntry(
                    matcher.group(1),
                    "-".equals(matcher.group(2)) ? null : matcher.group(2),
                    parseDateTime(matcher.group(3)),
                    matcher.group(4),
                    Integer.parseInt(matcher.group(5)),
                    Long.parseLong(matcher.group(6)),
                    "-".equals(matcher.group(7)) ? null : matcher.group(7),
                    "-".equals(matcher.group(8)) ? null : matcher.group(8),
                    "",
                    "");

            return Optional.of(logEntry);
        } catch (Exception e) {
            logger.warn("Failed to parse log line: {}", line, e);
            return Optional.empty();
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, DATE_FORMATTER);
    }
}
