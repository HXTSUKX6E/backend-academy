package academy.transformations.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SinusoidalTransformationTest {

    @Test
    @DisplayName("Трансформация корректно вычисляет новые координаты")
    void testApply() {
        SinusoidalTransformation t = new SinusoidalTransformation();

        double x = Math.PI / 2;
        double y = Math.PI / 6;

        double expectedX = Math.sin(x);
        double expectedY = Math.sin(y);

        double[] result = t.apply(x, y);

        assertEquals(expectedX, result[0], 1e-9);
        assertEquals(expectedY, result[1], 1e-9);
    }

    @Test
    @DisplayName("Повторный вызов с одинаковыми аргументами даёт одинаковый результат")
    void testConsistency() {
        SinusoidalTransformation t = new SinusoidalTransformation();

        double[] r1 = t.apply(0.3, -0.7);
        double[] r2 = t.apply(0.3, -0.7);

        assertArrayEquals(r1, r2, 1e-12);
    }

    @Test
    @DisplayName("Результат трансформации содержит конечные числа")
    void testFiniteResults() {
        SinusoidalTransformation t = new SinusoidalTransformation();

        double[] r = t.apply(1.2, -0.8);

        assertTrue(Double.isFinite(r[0]));
        assertTrue(Double.isFinite(r[1]));
    }

    @Test
    @DisplayName("Трансформация в нуле даёт нулевой результат")
    void testZeroInput() {
        SinusoidalTransformation t = new SinusoidalTransformation();

        double[] r = t.apply(0.0, 0.0);

        assertEquals(0.0, r[0], 1e-12);
        assertEquals(0.0, r[1], 1e-12);
    }

    @Test
    @DisplayName("Название трансформации корректно")
    void testName() {
        SinusoidalTransformation t = new SinusoidalTransformation();

        assertEquals("sinusoidal", t.name());
    }

    @Test
    @DisplayName("Трансформация ограничивает выходные значения диапазоном [-1, 1]")
    void testOutputRange() {
        SinusoidalTransformation t = new SinusoidalTransformation();

        double[][] testCases = {
            {10.0, 5.0},
            {-Math.PI, 2 * Math.PI},
            {100.0, -100.0}
        };

        for (double[] testCase : testCases) {
            double[] result = t.apply(testCase[0], testCase[1]);
            assertTrue(Math.abs(result[0]) <= 1.0 + 1e-12);
            assertTrue(Math.abs(result[1]) <= 1.0 + 1e-12);
        }
    }
}
