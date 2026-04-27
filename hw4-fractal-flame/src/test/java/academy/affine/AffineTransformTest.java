package academy.affine;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AffineTransformTest {
    @Test
    @DisplayName("Корректное вычисление координат при произвольных коэффициентах")
    void test1() {
        AffineTransform transform = new AffineTransform(2.0, 3.0, 4.0, 5.0, 6.0, 7.0);

        double[] result = transform.apply(1.0, 2.0);

        assertEquals(2.0 + 3.0 * 2.0 + 4.0, result[0], 1e-9);
        assertEquals(5.0 + 6.0 * 2.0 + 7.0, result[1], 1e-9);
    }

    @Test
    @DisplayName("Единичное преобразование не изменяет координаты")
    void test2() {
        AffineTransform transform = new AffineTransform(1.0, 0.0, 0.0, 0.0, 1.0, 0.0);

        double[] result = transform.apply(3.5, -7.2);

        assertEquals(3.5, result[0], 1e-9);
        assertEquals(-7.2, result[1], 1e-9);
    }

    @Test
    @DisplayName("Нулевые коэффициенты дают константный сдвиг")
    void test3() {
        AffineTransform transform = new AffineTransform(0.0, 0.0, 10.0, 0.0, 0.0, -5.0);

        double[] result = transform.apply(100.0, -200.0);

        assertEquals(10.0, result[0], 1e-9);
        assertEquals(-5.0, result[1], 1e-9);
    }

    @Test
    @DisplayName("Корректная работа с отрицательными значениями")
    void test4() {
        AffineTransform transform = new AffineTransform(-1.0, 2.0, -3.0, 4.0, -5.0, 6.0);

        double[] result = transform.apply(-2.0, 3.0);

        assertEquals(-1.0 * -2.0 + 2.0 * 3.0 - 3.0, result[0], 1e-9);
        assertEquals(4.0 * -2.0 - 5.0 * 3.0 + 6.0, result[1], 1e-9);
    }
}
