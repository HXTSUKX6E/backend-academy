package academy.transformations.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SphericalTransformationTest {

    @Test
    @DisplayName("Трансформация корректно вычисляет новые координаты")
    void testApply() {
        SphericalTransformation t = new SphericalTransformation();

        double x = 1.0;
        double y = 0.5;

        double r2 = x * x + y * y + 1e-6;
        double expectedX = x / r2;
        double expectedY = y / r2;

        double[] result = t.apply(x, y);

        assertEquals(expectedX, result[0], 1e-9);
        assertEquals(expectedY, result[1], 1e-9);
    }

    @Test
    @DisplayName("Повторный вызов с одинаковыми аргументами даёт одинаковый результат")
    void testConsistency() {
        SphericalTransformation t = new SphericalTransformation();

        double[] r1 = t.apply(0.3, -0.7);
        double[] r2 = t.apply(0.3, -0.7);

        assertArrayEquals(r1, r2, 1e-12);
    }

    @Test
    @DisplayName("Результат трансформации содержит конечные числа")
    void testFiniteResults() {
        SphericalTransformation t = new SphericalTransformation();

        double[] r = t.apply(0.8, -0.6);

        assertTrue(Double.isFinite(r[0]));
        assertTrue(Double.isFinite(r[1]));
    }

    @Test
    @DisplayName("Трансформация в нуле даёт нулевой результат")
    void testZeroInput() {
        SphericalTransformation t = new SphericalTransformation();

        double[] r = t.apply(0.0, 0.0);

        assertEquals(0.0, r[0], 1e-12);
        assertEquals(0.0, r[1], 1e-12);
    }

    @Test
    @DisplayName("Название трансформации корректно")
    void testName() {
        SphericalTransformation t = new SphericalTransformation();

        assertEquals("spherical", t.name());
    }

    @Test
    @DisplayName("Трансформация инвертирует расстояние от центра")
    void testInversion() {
        SphericalTransformation t = new SphericalTransformation();

        double[] r1 = t.apply(0.1, 0.0);
        double[] r2 = t.apply(1.0, 0.0);

        assertTrue(Math.abs(r1[0]) > Math.abs(r2[0]));
    }
}
