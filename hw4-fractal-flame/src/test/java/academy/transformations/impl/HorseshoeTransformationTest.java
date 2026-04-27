package academy.transformations.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HorseshoeTransformationTest {

    @Test
    @DisplayName("Трансформация корректно вычисляет новые координаты")
    void testApply() {
        HorseshoeTransformation t = new HorseshoeTransformation();

        double x = 1.0;
        double y = 0.5;

        double r = Math.sqrt(x * x + y * y) + 1e-6;
        double expectedX = (x - y) * (x + y) / r;
        double expectedY = 2 * x * y / r;

        double[] result = t.apply(x, y);

        assertEquals(expectedX, result[0], 1e-9);
        assertEquals(expectedY, result[1], 1e-9);
    }

    @Test
    @DisplayName("Повторный вызов с одинаковыми аргументами даёт одинаковый результат")
    void testConsistency() {
        HorseshoeTransformation t = new HorseshoeTransformation();

        double[] r1 = t.apply(0.3, -0.7);
        double[] r2 = t.apply(0.3, -0.7);

        assertArrayEquals(r1, r2, 1e-12);
    }

    @Test
    @DisplayName("Результат трансформации содержит конечные числа")
    void testFiniteResults() {
        HorseshoeTransformation t = new HorseshoeTransformation();

        double[] r = t.apply(0.8, -0.6);

        assertTrue(Double.isFinite(r[0]));
        assertTrue(Double.isFinite(r[1]));
    }

    @Test
    @DisplayName("Трансформация в нуле даёт нулевой результат")
    void testZeroInput() {
        HorseshoeTransformation t = new HorseshoeTransformation();

        double[] r = t.apply(0.0, 0.0);

        assertEquals(0.0, r[0], 1e-12);
        assertEquals(0.0, r[1], 1e-12);
    }

    @Test
    @DisplayName("Название трансформации корректно")
    void testName() {
        HorseshoeTransformation t = new HorseshoeTransformation();

        assertEquals("horseshoe", t.name());
    }

    @Test
    @DisplayName("Симметрия относительно замены x и y")
    void testSymmetry() {
        HorseshoeTransformation t = new HorseshoeTransformation();

        double[] r1 = t.apply(1.0, 0.5);
        double[] r2 = t.apply(0.5, 1.0);

        // Проверяем, что x-координаты противоположны по знаку
        assertEquals(-r1[0], r2[0], 1e-9);

        assertEquals(r1[1], r2[1], 1e-9);
    }
}
