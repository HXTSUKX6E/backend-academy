package academy.transformations.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LinearTransformationTest {

    @Test
    @DisplayName("Трансформация возвращает те же координаты")
    void testApply() {
        LinearTransformation t = new LinearTransformation();

        double x = 1.5;
        double y = -0.7;

        double[] result = t.apply(x, y);

        assertEquals(x, result[0], 1e-12);
        assertEquals(y, result[1], 1e-12);
    }

    @Test
    @DisplayName("Повторный вызов с одинаковыми аргументами даёт одинаковый результат")
    void testConsistency() {
        LinearTransformation t = new LinearTransformation();

        double[] r1 = t.apply(0.3, -0.2);
        double[] r2 = t.apply(0.3, -0.2);

        assertArrayEquals(r1, r2, 1e-12);
    }

    @Test
    @DisplayName("Результат трансформации содержит конечные числа")
    void testFiniteResults() {
        LinearTransformation t = new LinearTransformation();

        double[] r = t.apply(2.1, -1.8);

        assertTrue(Double.isFinite(r[0]));
        assertTrue(Double.isFinite(r[1]));
    }

    @Test
    @DisplayName("Трансформация в нуле даёт нулевой результат")
    void testZeroInput() {
        LinearTransformation t = new LinearTransformation();

        double[] r = t.apply(0.0, 0.0);

        assertEquals(0.0, r[0], 1e-12);
        assertEquals(0.0, r[1], 1e-12);
    }

    @Test
    @DisplayName("Название трансформации корректно")
    void testName() {
        LinearTransformation t = new LinearTransformation();

        assertEquals("linear", t.name());
    }

    @Test
    @DisplayName("Трансформация сохраняет входные значения неизменными")
    void testIdentity() {
        LinearTransformation t = new LinearTransformation();

        double[][] testCases = {
            {1.0, 0.0},
            {0.0, 1.0},
            {-1.5, 2.3},
            {0.001, -0.002}
        };

        for (double[] testCase : testCases) {
            double x = testCase[0];
            double y = testCase[1];
            double[] result = t.apply(x, y);
            assertEquals(x, result[0], 1e-12);
            assertEquals(y, result[1], 1e-12);
        }
    }
}
