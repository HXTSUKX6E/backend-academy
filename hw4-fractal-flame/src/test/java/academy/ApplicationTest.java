package academy;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ApplicationTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Нормализация аргументов из одной строки")
    void testNormalizeArgsSingleString() {
        String[] input = {"--width 800 --height 600 --threads 4"};

        String[] result = Application.normalizeArgs(input);

        assertEquals(6, result.length);
        assertEquals("--width", result[0]);
        assertEquals("800", result[1]);
        assertEquals("--height", result[2]);
        assertEquals("600", result[3]);
        assertEquals("--threads", result[4]);
        assertEquals("4", result[5]);
    }

    @Test
    @DisplayName("Нормализация аргументов с лишними пробелами")
    void testNormalizeArgsWithExtraSpaces() {
        String[] input = {"  --width  800  --height  600  "};

        String[] result = Application.normalizeArgs(input);

        assertEquals(4, result.length);
        assertEquals("--width", result[0]);
        assertEquals("800", result[1]);
        assertEquals("--height", result[2]);
        assertEquals("600", result[3]);
    }

    @Test
    @DisplayName("Нормализация уже разделенных аргументов")
    void testNormalizeArgsAlreadySplit() {
        String[] input = {"--width", "800", "--height", "600"};

        String[] result = Application.normalizeArgs(input);

        assertArrayEquals(input, result);
    }

    @Test
    @DisplayName("Нормализация пустого массива")
    void testNormalizeArgsEmptyArray() {
        String[] input = {};

        String[] result = Application.normalizeArgs(input);

        assertArrayEquals(input, result);
    }

    @Test
    @DisplayName("Нормализация массива с одной пустой строкой")
    void testNormalizeArgsSingleEmptyString() {
        String[] input = {""};

        String[] result = Application.normalizeArgs(input);

        assertArrayEquals(input, result);
    }

    @Test
    @DisplayName("Нормализация строки без пробелов")
    void testNormalizeArgsNoSpaces() {
        String[] input = {"--config=config.json"};

        String[] result = Application.normalizeArgs(input);

        assertArrayEquals(input, result);
    }

    @Test
    @DisplayName("Основной метод с JSON конфигом")
    void testMainWithJsonConfig(@TempDir Path tempDir) throws IOException {
        String jsonConfig =
                """
            {
                "size": {
                    "width": 800,
                    "height": 600
                },
                "iteration_count": 100000,
                "threads": 1,
                "seed": 12345,
                "output_path": "test_output.png",
                "functions": [
                    {
                        "name": "linear",
                        "weight": 1.0,
                        "r": 255,
                        "g": 255,
                        "b": 255
                    }
                ],
                "affine_params": [
                    {
                        "a": 0.5,
                        "b": 0.0,
                        "c": 0.0,
                        "d": 0.5,
                        "e": 0.0,
                        "f": 0.0
                    }
                ]
            }
            """;

        Path configFile = tempDir.resolve("config.json");
        Files.writeString(configFile, jsonConfig);

        String[] args = {"--config", configFile.toString()};

        assertDoesNotThrow(() -> Application.main(args));
    }

    @Test
    @DisplayName("Основной метод с CLI аргументами")
    void testMainWithCliArgs() {
        String[] args = {
            "--width",
            "1800",
            "--height",
            "1800",
            "--iteration-count",
            "100000000",
            "--threads",
            "4",
            "--seed",
            "911999",
            "--symmetry-level",
            "2",
            "--gamma-correction",
            "--gamma",
            "2.2",
            "--output-path",
            "test.png",
            "--functions",
            "swirl:1.0,spiral:1.2,disc:1.0",
            "--affine-params",
            "0.8,0.0,0.0,0.8,-0.1,0.0;" + "-0.6,0.0,0.0,0.6,1.0,0.0;" + "0.5,0.0,0.0,-0.5,0.5,0.866"
        };

        assertDoesNotThrow(() -> Application.main(args));
    }
}
