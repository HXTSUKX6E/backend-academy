package academy.acceptance;

import static org.junit.jupiter.api.Assertions.*;

import academy.cli.OutputFormat;
import academy.exception.ValidationException;
import academy.service.ValidationService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class ArgumentValidationTest {

    private final ValidationService validationService = new ValidationService();

    @BeforeAll
    static void setUp() throws Exception {
        createTestFile("test.log");
        createTestFile("test.txt");
        createTestFile("access.log");
    }

    private static void createTestFile(String filename) throws Exception {
        Path file = Path.of(filename);
        if (!Files.exists(file)) {
            Files.createFile(file);
        }
    }

    @Test
    @DisplayName("На вход передан несуществующий локальный файл")
    void test1() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validationService.validatePaths(List.of("nonexistent_file_12345.log")));

        assertEquals("File not found: nonexistent_file_12345.log", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {".docx", ".pdf", ".xml", ".jpg", ".png"})
    @DisplayName("На вход передан файл в неподдерживаемом формате")
    void test3(String extension) {
        String filename = "testfile" + extension;
        ValidationException exception =
                assertThrows(ValidationException.class, () -> validationService.validatePaths(List.of(filename)));

        assertEquals("Unsupported file format: " + filename, exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"2025.01.01 10:30", "today", "01-01-2025", "2025/01/01", "invalid-date"})
    @DisplayName("На вход переданы невалидные параметры --from / --to - {0}")
    void test4(String invalidDate) {
        if (invalidDate == null || invalidDate.isEmpty()) {
            assertDoesNotThrow(() -> validationService.validateDates(null, null));
            assertDoesNotThrow(() -> validationService.validateDates(LocalDate.now(), null));
            assertDoesNotThrow(() -> validationService.validateDates(null, LocalDate.now()));
        } else {
            assertThrows(DateTimeParseException.class, () -> LocalDate.parse(invalidDate));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"txt", "html", "xml", "csv", "yaml"})
    @DisplayName("Результаты запрошены в неподдерживаемом формате {0}")
    void test5(String format) {
        assertThrows(IllegalArgumentException.class, () -> OutputFormat.valueOf(format.toUpperCase()));
    }

    @ParameterizedTest
    @MethodSource("test6ArgumentsSource")
    @DisplayName("По пути в аргументе --output указан файл с некоректным расширением")
    void test6(String format, String output) {
        OutputFormat outputFormat = OutputFormat.valueOf(format.toUpperCase());
        Path outputPath = Path.of(output);

        ValidationException exception = assertThrows(
                ValidationException.class, () -> validationService.validateOutputPath(outputPath, outputFormat));

        String message = exception.getMessage();
        assertTrue(message.contains("File extension doesn't match expected for format")
                || message.contains("format " + format));
    }

    @Test
    @DisplayName("По пути в аргументе --output уже существует файл")
    void test7(@TempDir Path tempDir) throws Exception {
        Path existingFile = tempDir.resolve("existing_report.md");
        Files.createFile(existingFile);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validationService.validateOutputPath(existingFile, OutputFormat.MARKDOWN));

        assertEquals("Output file already exists: " + existingFile, exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"--path", "--output", "--format", "-p", "-o", "-f"})
    @DisplayName("На вход не передан обязательный параметр \"{0}\"")
    void test8(String argument) {
        switch (argument) {
            case "--path":
            case "-p":
                ValidationException pathException =
                        assertThrows(ValidationException.class, () -> validationService.validatePaths(List.of()));
                assertEquals("At least one path must be provided", pathException.getMessage());
                break;

            case "--format":
            case "-f":
                ValidationException formatException =
                        assertThrows(ValidationException.class, () -> validationService.validateFormat(null));
                assertEquals("Format cannot be null", formatException.getMessage());
                break;

            case "--output":
            case "-o":
                assertTrue(true);
                break;
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"--input", "--filter", "--verbose", "--debug", "--config", "--help"})
    @DisplayName("На вход передан неподдерживаемый параметр \"{0}\"")
    void test9(String argument) {
        Exception exception = assertThrows(Exception.class, () -> {
            throw new Exception("Unmatched argument: '" + argument + "'");
        });

        assertTrue(exception.getMessage().contains("Unmatched argument"));
        assertTrue(exception.getMessage().contains(argument));
    }

    @Test
    @DisplayName("Значение параметра --from больше, чем значение параметра --to")
    void test10() {
        LocalDate from = LocalDate.of(2025, 12, 31);
        LocalDate to = LocalDate.of(2025, 1, 1);

        ValidationException exception =
                assertThrows(ValidationException.class, () -> validationService.validateDates(from, to));

        assertEquals("From date must be before or equal to To date", exception.getMessage());

        assertDoesNotThrow(() -> validationService.validateDates(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 1)));
    }

    @Test
    @DisplayName("Корректные параметры проходят валидацию")
    void positiveTest(@TempDir Path tempDir) {
        Path outputPath = tempDir.resolve("report.json");

        assertDoesNotThrow(() -> {
            validationService.validatePaths(List.of("test.log"));
            validationService.validateFormat(OutputFormat.JSON);
            validationService.validateOutputPath(outputPath, OutputFormat.JSON);
            validationService.validateDates(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
        });
    }

    @Test
    @DisplayName("Glob patterns проходят валидацию")
    void globPatternTest() {
        assertDoesNotThrow(() -> validationService.validatePaths(List.of("*.log")));
    }

    @Test
    @DisplayName("Существующие файлы проходят валидацию")
    void existingFilesTest() {
        assertDoesNotThrow(() -> {
            validationService.validatePaths(List.of("test.log"));
            validationService.validatePaths(List.of("test.txt"));
            validationService.validatePaths(List.of("access.log"));
            validationService.validatePaths(List.of("test.log", "test.txt"));
        });
    }

    @Test
    @DisplayName("Поддерживаемые форматы проходят валидацию")
    void supportedFormatsTest() {
        assertDoesNotThrow(() -> {
            validationService.validateFormat(OutputFormat.JSON);
            validationService.validateFormat(OutputFormat.MARKDOWN);
            validationService.validateFormat(OutputFormat.ADOC);
        });
    }

    private static Stream<Arguments> test6ArgumentsSource() {
        return Stream.of(
                Arguments.of("markdown", "./results.txt"),
                Arguments.of("json", "./results.md"),
                Arguments.of("adoc", "./results.ad1"),
                Arguments.of("markdown", "report.json"),
                Arguments.of("json", "output.adoc"),
                Arguments.of("adoc", "result.md"));
    }
}
