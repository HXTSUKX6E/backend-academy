package academy.service;

import academy.cli.OutputFormat;
import academy.exception.ValidationException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class ValidationService {

    public void validatePaths(List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            throw new ValidationException("At least one path must be provided");
        }

        for (String path : paths) {
            if (path == null || path.trim().isEmpty()) {
                throw new ValidationException("Path cannot be null or empty");
            }

            if (!isValidLogFileExtension(path)) {
                throw new ValidationException("Unsupported file format: " + path);
            }

            if (!isValidPath(path)) {
                throw new ValidationException("File not found: " + path);
            }
        }
    }

    public void validateFormat(OutputFormat format) {
        if (format == null) {
            throw new ValidationException("Format cannot be null");
        }
    }

    public void validateOutputPath(Path output, OutputFormat format) {
        if (output == null) {
            throw new ValidationException("Output path cannot be null");
        }

        Path fileNamePath = output.getFileName();
        if (fileNamePath == null) {
            throw new ValidationException("Output path must contain a file name");
        }

        if (Files.exists(output)) {
            throw new ValidationException("Output file already exists: " + output);
        }

        Path parentDir = output.getParent();
        if (parentDir != null && !Files.isWritable(parentDir)) {
            throw new ValidationException("Directory is not writable: " + parentDir);
        }

        String fileName = fileNamePath.toString();
        String expectedExtension = format.getFileExtension();

        if (!fileName.endsWith(expectedExtension)) {
            throw new ValidationException("File extension doesn't match expected for format " + format);
        }
    }

    public void validateDates(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ValidationException("From date must be before or equal to To date");
        }
    }

    private boolean isValidPath(String path) {
        try {
            if (isHttpUrl(path)) {
                new URL(path);
                return true;
            } else if (isGlobPattern(path)) {
                validateGlobSyntax(path);
                return true;
            } else {
                return Files.exists(Path.of(path));
            }
        } catch (MalformedURLException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isHttpUrl(String path) {
        return path.startsWith("http://") || path.startsWith("https://");
    }

    private void validateGlobSyntax(String pattern) {
        if (pattern.contains("**") && pattern.indexOf("**") != pattern.lastIndexOf("**")) {
            throw new IllegalArgumentException("Invalid glob pattern: multiple '**' not allowed");
        }

        int openBrackets = 0;
        for (char c : pattern.toCharArray()) {
            if (c == '[') openBrackets++;
            if (c == ']') openBrackets--;
            if (openBrackets < 0) {
                throw new IllegalArgumentException("Invalid glob pattern: unmatched ']'");
            }
        }
        if (openBrackets > 0) {
            throw new IllegalArgumentException("Invalid glob pattern: unmatched '['");
        }
    }

    private boolean isValidLogFileExtension(String path) {
        if (isHttpUrl(path) || isGlobPattern(path)) {
            return true;
        }
        return path.endsWith(".log") || path.endsWith(".txt");
    }

    private boolean isGlobPattern(String path) {
        return path.contains("*") || path.contains("?") || path.contains("[");
    }
}
