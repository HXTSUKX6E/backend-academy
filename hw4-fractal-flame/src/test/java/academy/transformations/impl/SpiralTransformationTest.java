package academy.transformations.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SpiralTransformationTest {

    @Test
    @DisplayName("Трансформация корректно вычисляет новые координаты")
    void testApply() {
        SpiralTransformation t = new SpiralTransformation();

        double x = 1.0;
        double y = 0.5;

        double r = Math.sqrt(x * x + y * y) + 1e-6;
        double theta = Math.atan2(y, x);
        double expectedX = (Math.cos(theta) + Math.sin(r)) / r;
        double expectedY = (Math.sin(theta) - Math.cos(r)) / r;

        double[] result = t.apply(x, y);

        assertEquals(expectedX, result[0], 1e-9);
        assertEquals(expectedY, result[1], 1e-9);
    }

    @Test
    @DisplayName("Повторный вызов с одинаковыми аргументами даёт одинаковый результат")
    void testConsistency() {
        SpiralTransformation t = new SpiralTransformation();

        double[] r1 = t.apply(0.3, -0.7);
        double[] r2 = t.apply(0.3, -0.7);

        assertArrayEquals(r1, r2, 1e-12);
    }

    @Test
    @DisplayName("Результат трансформации содержит конечные числа")
    void testFiniteResults() {
        SpiralTransformation t = new SpiralTransformation();

        double[] r = t.apply(0.8, -0.6);

        assertTrue(Double.isFinite(r[0]));
        assertTrue(Double.isFinite(r[1]));
    }

    @Test
    @DisplayName("Трансформация в нуле даёт конечный результат")
    void testZeroInput() {
        SpiralTransformation t = new SpiralTransformation();

        double[] r = t.apply(0.0, 0.0);

        assertTrue(Double.isFinite(r[0]));
        assertTrue(Double.isFinite(r[1]));
    }

    @Test
    @DisplayName("Название трансформации корректно")
    void testName() {
        SpiralTransformation t = new SpiralTransformation();

        assertEquals("spiral", t.name());
    }

    @Test
    @DisplayName("Трансформация создает спиралевидный паттерн")
    void testSpiralPattern() {
        SpiralTransformation t = new SpiralTransformation();

        double[] r1 = t.apply(0.1, 0.0);
        double[] r2 = t.apply(0.2, 0.0);
        double[] r3 = t.apply(0.3, 0.0);

        assertNotEquals(r1[0], r2[0], 1e-9);
        assertNotEquals(r2[0], r3[0], 1e-9);
        assertNotEquals(r1[1], r2[1], 1e-9);
        assertNotEquals(r2[1], r3[1], 1e-9);
    }
}
