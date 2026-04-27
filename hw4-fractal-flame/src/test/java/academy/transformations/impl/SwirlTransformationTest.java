package academy.transformations.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SwirlTransformationTest {

    @Test
    @DisplayName("Трансформация корректно вычисляет новые координаты")
    void testApply() {
        SwirlTransformation t = new SwirlTransformation();

        double x = 1.0;
        double y = 0.5;

        double r2 = x * x + y * y;
        double expectedX = x * Math.sin(r2) - y * Math.cos(r2);
        double expectedY = x * Math.cos(r2) + y * Math.sin(r2);

        double[] result = t.apply(x, y);

        assertEquals(expectedX, result[0], 1e-9);
        assertEquals(expectedY, result[1], 1e-9);
    }

    @Test
    @DisplayName("Повторный вызов с одинаковыми аргументами даёт одинаковый результат")
    void testConsistency() {
        SwirlTransformation t = new SwirlTransformation();

        double[] r1 = t.apply(0.3, -0.7);
        double[] r2 = t.apply(0.3, -0.7);

        assertArrayEquals(r1, r2, 1e-12);
    }

    @Test
    @DisplayName("Результат трансформации содержит конечные числа")
    void testFiniteResults() {
        SwirlTransformation t = new SwirlTransformation();

        double[] r = t.apply(0.8, -0.6);

        assertTrue(Double.isFinite(r[0]));
        assertTrue(Double.isFinite(r[1]));
    }

    @Test
    @DisplayName("Трансформация в нуле даёт нулевой результат")
    void testZeroInput() {
        SwirlTransformation t = new SwirlTransformation();

        double[] r = t.apply(0.0, 0.0);

        assertEquals(0.0, r[0], 1e-12);
        assertEquals(0.0, r[1], 1e-12);
    }

    @Test
    @DisplayName("Название трансформации корректно")
    void testName() {
        SwirlTransformation t = new SwirlTransformation();

        assertEquals("swirl", t.name());
    }

    @Test
    @DisplayName("Трансформация создает вихревой эффект")
    void testSwirlEffect() {
        SwirlTransformation t = new SwirlTransformation();

        double[] r1 = t.apply(0.1, 0.0);
        double[] r2 = t.apply(0.2, 0.0);

        assertNotEquals(r1[0], r2[0], 1e-9);
        assertNotEquals(r1[1], r2[1], 1e-9);
    }
}
