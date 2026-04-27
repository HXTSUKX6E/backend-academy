package academy.service;

import academy.model.LogEntry;
import academy.model.Statistics;
import academy.model.Statistics.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogAnalyzer {

    public Statistics analyze(
            Stream<LogEntry> logEntriesStream, List<String> files, LocalDate fromDate, LocalDate toDate) {

        Predicate<LogEntry> dateFilter = createDateFilter(fromDate, toDate);

        List<LogEntry> filteredEntries = logEntriesStream.filter(dateFilter).collect(Collectors.toList());

        return analyzeList(filteredEntries, files);
    }

    private Statistics analyzeList(List<LogEntry> entries, List<String> files) {
        return new Statistics(
                files,
                entries.size(),
                calculateResponseSize(entries),
                calculateTopResources(entries),
                calculateResponseCodes(entries),
                calculateRequestsPerDate(entries),
                calculateUniqueProtocols(entries));
    }

    private Predicate<LogEntry> createDateFilter(LocalDate fromDate, LocalDate toDate) {
        return entry -> {
            LocalDate entryDate = entry.timeLocal().toLocalDate();
            return (fromDate == null || !entryDate.isBefore(fromDate))
                    && (toDate == null || !entryDate.isAfter(toDate));
        };
    }

    private ResponseSize calculateResponseSize(List<LogEntry> entries) {
        if (entries.isEmpty()) {
            return new ResponseSize(0, 0, 0);
        }

        List<Long> sizes =
                entries.stream().map(LogEntry::bodyBytesSent).sorted().collect(Collectors.toList());

        double average = sizes.stream().mapToLong(Long::longValue).average().orElse(0);
        double max = sizes.stream().mapToLong(Long::longValue).max().orElse(0);
        double p95 = calculatePercentile(sizes);

        return new ResponseSize(round(average), round(max), round(p95));
    }

    private List<ResourceStat> calculateTopResources(List<LogEntry> entries) {
        return entries.stream()
                .collect(Collectors.groupingBy(LogEntry::resource, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> new ResourceStat(entry.getKey(), entry.getValue().intValue()))
                .collect(Collectors.toList());
    }

    private List<ResponseCodeStat> calculateResponseCodes(List<LogEntry> entries) {
        return entries.stream()
                .collect(Collectors.groupingBy(LogEntry::status, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .map(entry ->
                        new ResponseCodeStat(entry.getKey(), entry.getValue().intValue()))
                .collect(Collectors.toList());
    }

    private List<DateStat> calculateRequestsPerDate(List<LogEntry> entries) {
        if (entries.isEmpty()) {
            return List.of();
        }

        Map<LocalDate, Long> requestsByDate = entries.stream()
                .collect(Collectors.groupingBy(entry -> entry.timeLocal().toLocalDate(), Collectors.counting()));

        int totalRequests = entries.size();

        return requestsByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    int count = entry.getValue().intValue();
                    double percentage = count * 100.0 / totalRequests;

                    return new DateStat(
                            date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH),
                            count,
                            round(percentage));
                })
                .collect(Collectors.toList());
    }

    private List<String> calculateUniqueProtocols(List<LogEntry> entries) {
        return entries.stream()
                .map(LogEntry::getProtocol)
                .filter(protocol -> protocol != null && !protocol.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    private double calculatePercentile(List<Long> values) {
        if (values.isEmpty()) return 0;

        double index = (double) 95 / 100.0 * (values.size() - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);

        if (lower == upper) {
            return values.get(lower);
        }

        double weight = index - lower;
        return values.get(lower) * (1 - weight) + values.get(upper) * weight;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
