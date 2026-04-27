package academy.transformations.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CurlTransformationTest {

    @Test
    @DisplayName("Трансформация корректно вычисляет новые координаты")
    void test2() {
        CurlTransformation t = new CurlTransformation(0.5, 0.2);

        double x = 1.0;
        double y = 0.5;

        double r2 = x * x + y * y;
        double denom = 1.0 + 0.5 * x + 0.2 * (x * x - y * y);

        double expectedX = (x + 0.5 * r2) / denom;
        double expectedY = (y + 0.2 * r2) / denom;

        double[] result = t.apply(x, y);

        assertEquals(expectedX, result[0], 1e-9);
        assertEquals(expectedY, result[1], 1e-9);
    }

    @Test
    @DisplayName("Повторный вызов с одинаковыми аргументами даёт одинаковый результат")
    void test3() {
        CurlTransformation t = new CurlTransformation();

        double[] r1 = t.apply(0.3, -0.7);
        double[] r2 = t.apply(0.3, -0.7);

        assertArrayEquals(r1, r2, 1e-12);
    }

    @Test
    @DisplayName("Пользовательские параметры конструктора учитываются в вычислениях")
    void test4() {
        CurlTransformation t1 = new CurlTransformation(0.1, 0.1);
        CurlTransformation t2 = new CurlTransformation(1.0, 1.0);

        double[] r1 = t1.apply(0.5, 0.5);
        double[] r2 = t2.apply(0.5, 0.5);

        assertNotEquals(r1[0], r2[0], 1e-9);
        assertNotEquals(r1[1], r2[1], 1e-9);
    }

    @Test
    @DisplayName("Результат трансформации содержит конечные числа")
    void test5() {
        CurlTransformation t = new CurlTransformation();

        double[] r = t.apply(1.2, -0.8);

        assertTrue(Double.isFinite(r[0]));
        assertTrue(Double.isFinite(r[1]));
    }
}
