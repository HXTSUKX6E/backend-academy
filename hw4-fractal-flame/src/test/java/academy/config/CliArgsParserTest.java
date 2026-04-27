package academy.config;

import static org.junit.jupiter.api.Assertions.*;

import academy.AppConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CliArgsParserTest {

    @Test
    @DisplayName("Корректно парсятся ширина и высота")
    void test1() {
        AppConfig config = new AppConfig();

        String[] args = {"--width", "800", "--height", "600"};
        CliArgsParser.parse(config, args);

        assertEquals(800, config.width);
        assertEquals(600, config.height);
    }

    @Test
    @DisplayName("Корректно парсится количество итераций и число потоков")
    void test2() {
        AppConfig config = new AppConfig();

        String[] args = {"-i", "100000", "-t", "4"};
        CliArgsParser.parse(config, args);

        assertEquals(100000L, config.iterationCount);
        assertEquals(4, config.threads);
    }

    @Test
    @DisplayName("Корректно парсится seed и output path")
    void test3() {
        AppConfig config = new AppConfig();

        String[] args = {"--seed", "42", "-o", "result.png"};
        CliArgsParser.parse(config, args);

        assertEquals(42L, config.seed);
        assertEquals("result.png", config.outputPath);
    }

    @Test
    @DisplayName("Флаг gamma-correction корректно включается")
    void test4() {
        AppConfig config = new AppConfig();

        String[] args = {"--gamma-correction"};
        CliArgsParser.parse(config, args);

        assertTrue(config.gammaCorrection);
    }

    @Test
    @DisplayName("Корректно парсится значение gamma")
    void test5() {
        AppConfig config = new AppConfig();

        String[] args = {"--gamma", "2.2"};
        CliArgsParser.parse(config, args);

        assertEquals(2.2, config.gamma, 1e-9);
    }

    @Test
    @DisplayName("Уровень симметрии не может быть меньше 1")
    void test6() {
        AppConfig config = new AppConfig();

        String[] args = {"--symmetry-level", "0"};
        CliArgsParser.parse(config, args);

        assertEquals(1, config.symmetryLevel);
    }

    @Test
    @DisplayName("Неизвестный аргумент вызывает IllegalArgumentException")
    void test7() {
        AppConfig config = new AppConfig();

        String[] args = {"--unknown-option"};

        assertThrows(IllegalArgumentException.class, () -> CliArgsParser.parse(config, args));
    }
}
