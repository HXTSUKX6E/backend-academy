package academy.hangman.data;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ComplexityTest {

    @Test
    void testGetters_ReturnCorrectValues() {

        assertEquals("Легкий", Complexity.EASY.getDisplayName());
        assertEquals(8, Complexity.EASY.getMaxAttempts());

        assertEquals("Средний", Complexity.MEDIUM.getDisplayName());
        assertEquals(6, Complexity.MEDIUM.getMaxAttempts());

        assertEquals("Сложный", Complexity.HARD.getDisplayName());
        assertEquals(4, Complexity.HARD.getMaxAttempts());

        assertEquals("Случайная сложность", Complexity.RANDOM.getDisplayName());
        assertEquals(-1, Complexity.RANDOM.getMaxAttempts());

        assertEquals("Пользовательский", Complexity.CUSTOM.getDisplayName());
        assertEquals(-1, Complexity.CUSTOM.getMaxAttempts());
    }

    @RepeatedTest(20)
    void testGetRealRandomComplexity_ReturnsOnlyRealLevels() {
        // Метод не должен возвращать RANDOM или CUSTOM
        Set<Complexity> allowed = EnumSet.of(Complexity.EASY, Complexity.MEDIUM, Complexity.HARD);

        Complexity random = Complexity.getRealRandomComplexity();

        assertTrue(allowed.contains(random),
            "getRealRandomComplexity должен возвращать только EASY, MEDIUM или HARD");
    }

    @Test
    void testGetRealRandomComplexity_CoversAllPossibleValues() {

        Set<Complexity> results = EnumSet.noneOf(Complexity.class);

        for (int i = 0; i < 100; i++) {
            results.add(Complexity.getRealRandomComplexity());
        }

        assertTrue(results.contains(Complexity.EASY));
        assertTrue(results.contains(Complexity.MEDIUM));
        assertTrue(results.contains(Complexity.HARD));
    }

    @Test
    void testEnumCount_AndIntegrity() {

        Complexity[] values = Complexity.values();

        assertEquals(5, values.length, "Ожидается 5 констант в перечислении Complexity");

        for (Complexity c : values) {
            assertNotNull(c.getDisplayName(), "Name не должен быть null");
        }
    }
}
