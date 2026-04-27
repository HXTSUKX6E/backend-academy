package academy.utils;

import static org.junit.jupiter.api.Assertions.*;

import academy.transformations.WeightedTransformation;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FunctionParserTest {

    @Test
    @DisplayName("Парсинг одной функции с весом")
    void testParseSingleFunction() {
        String input = "linear:1.0";
        long seed = 12345L;

        List<WeightedTransformation> result = FunctionParser.parse(input, seed);

        assertEquals(1, result.size());
        WeightedTransformation wt = result.getFirst();
        assertEquals("linear", wt.transformation().name());
        assertEquals(1.0, wt.weight(), 1e-9);
    }

    @Test
    @DisplayName("Парсинг нескольких функций")
    void testParseMultipleFunctions() {
        String input = "linear:1.0,swirl:0.5,spherical:2.0";
        long seed = 12345L;

        List<WeightedTransformation> result = FunctionParser.parse(input, seed);

        assertEquals(3, result.size());
        assertEquals("linear", result.get(0).transformation().name());
        assertEquals("swirl", result.get(1).transformation().name());
        assertEquals("spherical", result.get(2).transformation().name());

        assertEquals(1.0, result.get(0).weight(), 1e-9);
        assertEquals(0.5, result.get(1).weight(), 1e-9);
        assertEquals(2.0, result.get(2).weight(), 1e-9);
    }

    @Test
    @DisplayName("Пробелы вокруг значений корректно обрабатываются")
    void testParseWithSpaces() {
        String input = "  linear : 1.0 , swirl : 0.5  ";
        long seed = 12345L;

        List<WeightedTransformation> result = FunctionParser.parse(input, seed);

        assertEquals(2, result.size());
        assertEquals("linear", result.get(0).transformation().name());
        assertEquals("swirl", result.get(1).transformation().name());
    }

    @Test
    @DisplayName("Некорректный формат вызывает исключение")
    void testParseInvalidFormat() {
        String input = "linear:1.0:extra";
        long seed = 12345L;

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> FunctionParser.parse(input, seed));

        assertTrue(exception.getMessage().contains("Invalid function format"));
    }

    @Test
    @DisplayName("Неизвестное имя трансформации вызывает исключение")
    void testParseUnknownTransformation() {
        String input = "unknown:1.0";
        long seed = 12345L;

        assertThrows(IllegalArgumentException.class, () -> FunctionParser.parse(input, seed));
    }

    @Test
    @DisplayName("Некорректный вес вызывает исключение")
    void testParseInvalidWeight() {
        String input = "linear:abc";
        long seed = 12345L;

        assertThrows(NumberFormatException.class, () -> FunctionParser.parse(input, seed));
    }

    @Test
    @DisplayName("Одинаковый seed дает одинаковые случайные цвета")
    void testSameSeedSameColors() {
        String input = "linear:1.0,swirl:0.5";
        long seed = 99999L;

        List<WeightedTransformation> result1 = FunctionParser.parse(input, seed);
        List<WeightedTransformation> result2 = FunctionParser.parse(input, seed);

        assertEquals(2, result1.size());
        assertEquals(2, result2.size());

        for (int i = 0; i < result1.size(); i++) {
            WeightedTransformation wt1 = result1.get(i);
            WeightedTransformation wt2 = result2.get(i);

            assertEquals(wt1.r(), wt2.r(), 1e-12);
            assertEquals(wt1.g(), wt2.g(), 1e-12);
            assertEquals(wt1.b(), wt2.b(), 1e-12);
        }
    }

    @Test
    @DisplayName("Разные seed дают разные случайные цвета")
    void testDifferentSeedDifferentColors() {
        String input = "linear:1.0";

        List<WeightedTransformation> result1 = FunctionParser.parse(input, 11111L);
        List<WeightedTransformation> result2 = FunctionParser.parse(input, 22222L);

        assertEquals(1, result1.size());
        assertEquals(1, result2.size());

        WeightedTransformation wt1 = result1.getFirst();
        WeightedTransformation wt2 = result2.getFirst();

        assertNotEquals(wt1.r(), wt2.r(), 1e-12);
        assertNotEquals(wt1.g(), wt2.g(), 1e-12);
        assertNotEquals(wt1.b(), wt2.b(), 1e-12);
    }
}
