package academy.utils;

import static org.junit.jupiter.api.Assertions.*;

import academy.affine.AffineTransform;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AffineParserTest {

    @Test
    @DisplayName("Парсинг одного аффинного преобразования")
    void testParseSingleAffine() {
        String input = "0.5,0.1,0.2,0.6,1.0,0.5";

        List<AffineTransform> result = AffineParser.parse(input);

        assertEquals(1, result.size());
        AffineTransform a = result.get(0);
        assertEquals(0.5, a.a(), 1e-9);
        assertEquals(0.1, a.b(), 1e-9);
        assertEquals(0.2, a.c(), 1e-9);
        assertEquals(0.6, a.d(), 1e-9);
        assertEquals(1.0, a.e(), 1e-9);
        assertEquals(0.5, a.f(), 1e-9);
    }

    @Test
    @DisplayName("Парсинг нескольких аффинных преобразований")
    void testParseMultipleAffines() {

        String input = "0.5,0.0,0.0,0.5,0.0,0.0;" + "0.5,0.0,0.0,0.5,1.0,0.0";

        List<AffineTransform> result = AffineParser.parse(input);

        assertEquals(2, result.size());

        AffineTransform a1 = result.getFirst();
        assertEquals(0.5, a1.a(), 1e-9);
        assertEquals(0.0, a1.e(), 1e-9);

        AffineTransform a2 = result.get(1);
        assertEquals(0.5, a2.a(), 1e-9);
        assertEquals(1.0, a2.e(), 1e-9);
    }

    @Test
    @DisplayName("Пустая строка возвращает пустой список")
    void testParseEmptyString() {
        String input = "";

        List<AffineTransform> result = AffineParser.parse(input);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Пробелы вокруг значений корректно обрабатываются")
    void testParseWithSpaces() {
        String input = " 0.5 , 0.1 , 0.2 , 0.6 , 1.0 , 0.5 ";

        List<AffineTransform> result = AffineParser.parse(input);

        assertEquals(1, result.size());
        AffineTransform a = result.get(0);
        assertEquals(0.5, a.a(), 1e-9);
        assertEquals(1.0, a.e(), 1e-9);
    }

    @Test
    @DisplayName("Некорректное количество параметров вызывает исключение")
    void testParseInvalidParameterCount() {
        String input = "0.5,0.1,0.2,0.6,1.0";

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> AffineParser.parse(input));

        assertTrue(exception.getMessage().contains("Invalid affine params"));
        assertTrue(exception.getMessage().contains("Expected: a,b,c,d,e,f"));
    }

    @Test
    @DisplayName("Некорректные числовые значения вызывают исключение")
    void testParseInvalidNumberFormat() {
        String input = "0.5,abc,0.2,0.6,1.0,0.5";

        assertThrows(NumberFormatException.class, () -> AffineParser.parse(input));
    }

    @Test
    @DisplayName("Null строка — ошибка")
    void testParseNull() {
        assertThrows(IllegalArgumentException.class, () -> AffineParser.parse(null));
    }
}
