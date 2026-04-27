package academy.transformations.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DiscTransformationTest {

    @Test
    @DisplayName("Трансформация корректно вычисляет новые координаты")
    void testApply() {
        DiscTransformation t = new DiscTransformation();

        double x = 0.3;
        double y = 0.4;

        double r = Math.sqrt(x * x + y * y);
        double theta = Math.atan2(y, x);
        double factor = theta / Math.PI;

        double expectedX = factor * Math.sin(Math.PI * r);
        double expectedY = factor * Math.cos(Math.PI * r);

        double[] result = t.apply(x, y);

        assertEquals(expectedX, result[0], 1e-9);
        assertEquals(expectedY, result[1], 1e-9);
    }

    @Test
    @DisplayName("Повторный вызов с одинаковыми аргументами даёт одинаковый результат")
    void testConsistency() {
        DiscTransformation t = new DiscTransformation();

        double[] r1 = t.apply(-0.2, 0.7);
        double[] r2 = t.apply(-0.2, 0.7);

        assertArrayEquals(r1, r2, 1e-12);
    }

    @Test
    @DisplayName("Результат трансформации содержит конечные числа")
    void testFiniteResults() {
        DiscTransformation t = new DiscTransformation();

        double[] r = t.apply(1.0, 0.0);

        assertTrue(Double.isFinite(r[0]));
        assertTrue(Double.isFinite(r[1]));
    }

    @Test
    @DisplayName("Трансформация в нуле даёт нулевой результат")
    void testZeroInput() {
        DiscTransformation t = new DiscTransformation();

        double[] r = t.apply(0.0, 0.0);

        assertEquals(0.0, r[0], 1e-12);
        assertEquals(0.0, Math.abs(r[1]), 1e-12);
    }
}
