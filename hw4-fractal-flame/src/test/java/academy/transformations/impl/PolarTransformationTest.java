package academy.transformations.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PolarTransformationTest {

    @Test
    @DisplayName("Трансформация корректно вычисляет новые координаты")
    void testApply() {
        PolarTransformation t = new PolarTransformation();

        double x = 1.0;
        double y = 1.0;

        double r = Math.sqrt(x * x + y * y);
        double theta = Math.atan2(y, x);
        double expectedX = theta / Math.PI;
        double expectedY = r - 1.0;

        double[] result = t.apply(x, y);

        assertEquals(expectedX, result[0], 1e-9);
        assertEquals(expectedY, result[1], 1e-9);
    }

    @Test
    @DisplayName("Повторный вызов с одинаковыми аргументами даёт одинаковый результат")
    void testConsistency() {
        PolarTransformation t = new PolarTransformation();

        double[] r1 = t.apply(0.3, -0.7);
        double[] r2 = t.apply(0.3, -0.7);

        assertArrayEquals(r1, r2, 1e-12);
    }

    @Test
    @DisplayName("Результат трансформации содержит конечные числа")
    void testFiniteResults() {
        PolarTransformation t = new PolarTransformation();

        double[] r = t.apply(0.8, -0.6);

        assertTrue(Double.isFinite(r[0]));
        assertTrue(Double.isFinite(r[1]));
    }

    @Test
    @DisplayName("Трансформация в нуле даёт корректный результат")
    void testZeroInput() {
        PolarTransformation t = new PolarTransformation();

        double[] r = t.apply(0.0, 0.0);

        assertEquals(0.0, r[0], 1e-12);
        assertEquals(-1.0, r[1], 1e-12);
    }

    @Test
    @DisplayName("Название трансформации корректно")
    void testName() {
        PolarTransformation t = new PolarTransformation();

        assertEquals("polar", t.name());
    }

    @Test
    @DisplayName("Трансформация сохраняет угловую симметрию")
    void testAngularSymmetry() {
        PolarTransformation t = new PolarTransformation();

        double[] r1 = t.apply(1.0, 0.0);
        double[] r2 = t.apply(0.0, 1.0);

        assertNotEquals(r1[0], r2[0], 1e-9);
        assertEquals(0.0, r1[1], 1e-9);
        assertEquals(0.0, r2[1], 1e-9);
    }
}
