package academy;

import academy.cli.OutputFormat;
import academy.cli.OutputFormatConverter;
import academy.model.LogEntry;
import academy.model.Statistics;
import academy.service.LogAnalyzer;
import academy.service.LogParser;
import academy.service.LogReader;
import academy.service.ValidationService;
import academy.service.report.CompositeReportGenerator;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "Application Example", version = "Example 1.0", mixinStandardHelpOptions = true)
public class Application implements Runnable {

    @CommandLine.Option(
            names = {"--path", "-p"},
            description = "Path to one or more NGINX log files (local files, glob patterns or URLs)",
            required = true,
            arity = "1..*",
            split = ",")
    private List<String> paths;

    @CommandLine.Option(
            names = {"--format", "-f"},
            description = "Output format: json, markdown, adoc",
            required = true,
            converter = OutputFormatConverter.class)
    private OutputFormat format;

    @CommandLine.Option(
            names = {"--output", "-o"},
            description = "Output file path",
            required = true)
    private Path output;

    @CommandLine.Option(
            names = {"--from"},
            description = "Start date in ISO8601 format (YYYY-MM-DD)")
    private LocalDate fromDate;

    @CommandLine.Option(
            names = {"--to"},
            description = "End date in ISO8601 format (YYYY-MM-DD)")
    private LocalDate toDate;

    private static final String UNDEFINED_PARAMETER = "undefined";

    private static final Logger logger = LogManager.getLogger(Application.class);
    private final ValidationService validationService = new ValidationService();
    private final LogReader logReader = new LogReader();
    private final LogParser logParser = new LogParser();
    private final LogAnalyzer logAnalyzer = new LogAnalyzer();
    private final CompositeReportGenerator reportGenerator = new CompositeReportGenerator();

    public static void main(String[] args) {

        debugArgs(Arrays.asList(args));

        CommandLine cmd = new CommandLine(new Application());

        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        try {
            validationService.validatePaths(paths);
            validationService.validateFormat(format);
            validationService.validateOutputPath(output, format);
            validationService.validateDates(fromDate, toDate);

            logger.info("Validation completed successfully");

            List<String> actualFiles = logReader.getActualFileNames(paths);
            logger.info("Actual files to process: {}", actualFiles);

            Statistics statistics;
            try (var lines = logReader.readLogs(paths)) {
                Stream<LogEntry> logEntriesStream =
                        lines.map(logParser::parseLine).flatMap(Optional::stream);

                statistics = logAnalyzer.analyze(logEntriesStream, actualFiles, fromDate, toDate);
            }

            logger.info("Analysis completed");

            reportGenerator.generateReport(statistics, format, output);

            logger.info("Report successfully generated to: {}", output);

        } catch (academy.exception.ValidationException e) {
            logger.error("Validation error: {}", e.getMessage(), e);
            throw new CommandLine.ParameterException(new CommandLine(this), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error occurred", e);
            throw new CommandLine.ExecutionException(new CommandLine(this), "Execution failed", e);
        }
    }

    public static int execute(String[] args) {
        debugArgs(Arrays.asList(args));
        CommandLine cmd = new CommandLine(new Application());
        return cmd.execute(args);
    }

    @Deprecated(forRemoval = true)
    private static void debugArgs(List<String> args) {
        var argsPerParam = getArgumentsPerParameter(args);
        System.out.printf("Входные параметры программы: %s%n", argsPerParam);

        logPaths("Пути к лог-файлам", argsPerParam, "p", "path");
        logPaths("Пути к отчетам", argsPerParam, "o", "output");
    }

    private static Map<String, List<String>> getArgumentsPerParameter(List<String> args) {
        var argsPerParameter = new HashMap<String, List<String>>();
        argsPerParameter.put(UNDEFINED_PARAMETER, new ArrayList<>());

        var queue = new ArrayDeque<>(args);
        String currentParameter = null;

        while (!queue.isEmpty()) {
            var element = queue.removeFirst();

            if (element.startsWith("-")) {
                String name;
                String value = null;

                int eqIndex = element.indexOf('=');
                if (eqIndex >= 0) {
                    String rawName = element.substring(0, eqIndex);
                    value = element.substring(eqIndex + 1);
                    name = rawName.startsWith("--")
                            ? rawName.substring(2)
                            : rawName.startsWith("-") ? rawName.substring(1) : rawName;
                } else {
                    // формат --path value или -p value
                    name = element.startsWith("--") ? element.substring(2) : element.substring(1);
                }

                currentParameter = name;
                argsPerParameter.putIfAbsent(currentParameter, new ArrayList<>());

                if (value != null && !value.isEmpty()) {
                    argsPerParameter.get(currentParameter).add(value);
                }
            } else {
                argsPerParameter
                        .get(Optional.ofNullable(currentParameter).orElse(UNDEFINED_PARAMETER))
                        .add(element);
            }
        }

        return argsPerParameter;
    }

    private static void logPaths(String description, Map<String, List<String>> argsPerParam, String... params) {
        var paths = new ArrayList<String>();
        for (var param : params) {
            paths.addAll(argsPerParam.getOrDefault(param, List.of()));
        }

        System.out.printf(
                "%s: %s%n",
                description,
                paths.stream()
                        .map(it -> {
                            if (it.startsWith("http://") || it.startsWith("https://")) {
                                return "url: " + it;
                            } else if (it.contains("*") || it.contains("?")) {
                                return "glob: " + it;
                            } else {
                                try {
                                    Path path = Path.of(it);
                                    boolean exists = Files.exists(path);
                                    return "path: %s, exists: %s".formatted(it, exists);
                                } catch (InvalidPathException e) {
                                    return "path: " + it + " (invalid path)";
                                }
                            }
                        })
                        .collect(Collectors.joining("; ")));
    }
}
