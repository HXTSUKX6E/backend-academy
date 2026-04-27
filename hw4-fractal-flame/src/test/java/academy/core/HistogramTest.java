package academy.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HistogramTest {

    @Test
    @DisplayName("Гистограмма корректно инициализируется с заданными размерами")
    void test1() {
        Histogram histogram = new Histogram(10, 20);

        assertEquals(10, histogram.width);
        assertEquals(20, histogram.height);

        assertEquals(0.0, histogram.maxDensity(), 1e-9);
    }

    @Test
    @DisplayName("Попадание в допустимые координаты увеличивает значения и плотность")
    void test2() {
        Histogram histogram = new Histogram(5, 5);

        histogram.hit(2, 3, 0.2, 0.3, 0.5);

        float[] cell = histogram.get(2, 3);

        assertEquals(0.2f, cell[0], 1e-6);
        assertEquals(0.3f, cell[1], 1e-6);
        assertEquals(0.5f, cell[2], 1e-6);
        assertEquals(1.0f, cell[3], 1e-6);
    }

    @Test
    @DisplayName("Попадание за пределы гистограммы игнорируется")
    void test3() {
        Histogram histogram = new Histogram(5, 5);

        histogram.hit(-1, 0, 1, 1, 1);
        histogram.hit(0, -1, 1, 1, 1);
        histogram.hit(5, 0, 1, 1, 1);
        histogram.hit(0, 5, 1, 1, 1);

        assertEquals(0.0, histogram.maxDensity(), 1e-9);
    }

    @Test
    @DisplayName("Гистограммы корректно суммируются")
    void test4() {
        Histogram h1 = new Histogram(3, 3);
        Histogram h2 = new Histogram(3, 3);

        h1.hit(1, 1, 0.5, 0.5, 0.5);
        h2.hit(1, 1, 0.5, 0.5, 0.5);

        h1.addFrom(h2);

        float[] cell = h1.get(1, 1);

        assertEquals(1.0f, cell[0], 1e-6);
        assertEquals(1.0f, cell[1], 1e-6);
        assertEquals(1.0f, cell[2], 1e-6);
        assertEquals(2.0f, cell[3], 1e-6);
    }

    @Test
    @DisplayName("Суммирование гистограмм с разными размерами вызывает исключение")
    void test5() {
        Histogram h1 = new Histogram(3, 3);
        Histogram h2 = new Histogram(4, 3);

        assertThrows(IllegalArgumentException.class, () -> h1.addFrom(h2));
    }

    @Test
    @DisplayName("Метод maxDensity возвращает максимальную плотность")
    void test6() {
        Histogram histogram = new Histogram(4, 4);

        histogram.hit(0, 0, 1, 1, 1);
        histogram.hit(0, 0, 1, 1, 1);
        histogram.hit(2, 2, 1, 1, 1);

        assertEquals(2.0, histogram.maxDensity(), 1e-9);
    }

    @Test
    @DisplayName("Метод get возвращает корректные значения ячейки")
    void test7() {
        Histogram histogram = new Histogram(2, 2);

        histogram.hit(1, 0, 0.1, 0.2, 0.3);

        float[] cell = histogram.get(1, 0);

        assertEquals(0.1f, cell[0], 1e-6);
        assertEquals(0.2f, cell[1], 1e-6);
        assertEquals(0.3f, cell[2], 1e-6);
        assertEquals(1.0f, cell[3], 1e-6);
    }

    @Test
    @DisplayName("Метод clear очищает всю гистограмму")
    void test8() {
        Histogram histogram = new Histogram(3, 3);

        histogram.hit(1, 1, 1, 1, 1);
        histogram.clear();

        assertEquals(0.0, histogram.maxDensity(), 1e-9);

        float[] cell = histogram.get(1, 1);
        assertEquals(0.0f, cell[0], 1e-6);
        assertEquals(0.0f, cell[1], 1e-6);
        assertEquals(0.0f, cell[2], 1e-6);
        assertEquals(0.0f, cell[3], 1e-6);
    }
}
