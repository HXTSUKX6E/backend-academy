package academy.service;

import academy.exception.LogAnalysisException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogReader {

    /**
     * Возвращает список реальных имён файлов (без путей), которые будут анализироваться. Для: - обычных путей: имя
     * файла; - glob-паттернов: имена всех файлов, подходящих под шаблон; - URL-ов: сам URL (или можно адаптировать под
     * требования задачи).
     */
    public List<String> getActualFileNames(List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            throw new LogAnalysisException("No paths provided");
        }

        List<String> result = new ArrayList<>();

        for (String path : paths) {
            if (path == null || path.isBlank()) {
                continue;
            }

            if (isHttpUrl(path)) {
                result.add(path);
            } else if (isGlobPattern(path)) {
                try {
                    List<Path> matchedFiles = expandGlobPattern(path);
                    if (matchedFiles.isEmpty()) {
                        throw new LogAnalysisException("No files match pattern: " + path);
                    }
                    for (Path file : matchedFiles) {
                        Path fileName = file.getFileName();
                        result.add(fileName != null ? fileName.toString() : file.toString());
                    }
                } catch (IOException e) {
                    throw new LogAnalysisException("Failed to expand glob pattern: " + path, e);
                }
            } else {
                try {
                    Path filePath = Path.of(path);
                    Path fileName = filePath.getFileName();
                    result.add(fileName != null ? fileName.toString() : path);
                } catch (Exception e) {
                    result.add(path);
                }
            }
        }

        return result.stream().distinct().sorted().collect(Collectors.toList());
    }

    public Stream<String> readLogs(List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            throw new LogAnalysisException("No paths provided");
        }

        return paths.stream().flatMap(this::readLogLines);
    }

    private Stream<String> readLogLines(String path) {
        if (isHttpUrl(path)) {
            return readFromUrl(path);
        } else if (isGlobPattern(path)) {
            return expandAndReadGlob(path);
        } else {
            return readFromFile(path);
        }
    }

    private Stream<String> readFromFile(String filePath) {
        try {
            Path path = Path.of(filePath);
            if (!Files.exists(path)) {
                throw new LogAnalysisException("File not found: " + filePath);
            }
            if (!Files.isReadable(path)) {
                throw new LogAnalysisException("File is not readable: " + filePath);
            }
            return Files.lines(path);
        } catch (IOException e) {
            throw new LogAnalysisException("Failed to read file: " + filePath, e);
        }
    }

    private Stream<String> readFromUrl(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            try (InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                List<String> lines = reader.lines().collect(Collectors.toList());
                return lines.stream();
            }
        } catch (IOException e) {
            throw new LogAnalysisException("Failed to read from URL: " + url, e);
        }
    }

    private Stream<String> expandAndReadGlob(String globPattern) {
        try {
            List<Path> matchedFiles = expandGlobPattern(globPattern);
            if (matchedFiles.isEmpty()) {
                throw new LogAnalysisException("No files match pattern: " + globPattern);
            }
            return matchedFiles.stream().flatMap(path -> {
                try {
                    return Files.lines(path);
                } catch (IOException e) {
                    throw new LogAnalysisException("Failed to read file: " + path, e);
                }
            });
        } catch (IOException e) {
            throw new LogAnalysisException("Failed to expand glob pattern: " + globPattern, e);
        }
    }

    private boolean isHttpUrl(String path) {
        return path.startsWith("http://") || path.startsWith("https://");
    }

    private boolean isGlobPattern(String path) {
        return path.contains("*") || path.contains("?") || path.contains("[");
    }

    /** Раскрывает glob-паттерн вида "/tmp/data/input/logs/*.txt" в список реальных файлов. */
    private List<Path> expandGlobPattern(String globPattern) throws IOException {
        if (globPattern == null || globPattern.isBlank()) {
            throw new IllegalArgumentException("Glob pattern cannot be null or empty");
        }

        Path patternPath = Path.of(globPattern).normalize();

        Path parentDir = patternPath.getParent();
        if (parentDir == null) {
            parentDir = Path.of(".").toAbsolutePath().normalize();
        }

        Path fileNamePath = patternPath.getFileName();
        if (fileNamePath == null) {
            throw new IllegalArgumentException("Invalid glob pattern: " + globPattern);
        }
        String fileNamePattern = fileNamePath.toString();

        if (!Files.exists(parentDir)) {
            throw new IOException("Directory not found: " + parentDir.toAbsolutePath());
        }

        List<Path> matchedFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(parentDir, fileNamePattern)) {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    matchedFiles.add(path.toAbsolutePath());
                }
            }
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid glob pattern syntax: " + fileNamePattern, e);
        }

        return matchedFiles;
    }
}
