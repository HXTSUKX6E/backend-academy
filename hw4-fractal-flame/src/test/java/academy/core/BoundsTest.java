package academy.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BoundsTest {

    @Test
    @DisplayName("Начальные границы установлены в максимальные значения")
    void test1() {
        Bounds bounds = new Bounds();

        assertEquals(Double.MAX_VALUE, bounds.minX());
        assertEquals(-Double.MAX_VALUE, bounds.maxX());
        assertEquals(Double.MAX_VALUE, bounds.minY());
        assertEquals(-Double.MAX_VALUE, bounds.maxY());
    }

    @Test
    @DisplayName("Границы корректно обновляются при добавлении точек")
    void test2() {
        Bounds bounds = new Bounds();

        bounds.update(1.0, 2.0);
        bounds.update(-3.0, 5.0);
        bounds.update(4.0, -1.0);

        assertEquals(-3.0, bounds.minX(), 1e-9);
        assertEquals(4.0, bounds.maxX(), 1e-9);
        assertEquals(-1.0, bounds.minY(), 1e-9);
        assertEquals(5.0, bounds.maxY(), 1e-9);
    }

    @Test
    @DisplayName("Корректно обрабатываются отрицательные координаты")
    void test3() {
        Bounds bounds = new Bounds();

        bounds.update(-10.0, -20.0);
        bounds.update(-5.0, -15.0);

        assertEquals(-10.0, bounds.minX(), 1e-9);
        assertEquals(-5.0, bounds.maxX(), 1e-9);
        assertEquals(-20.0, bounds.minY(), 1e-9);
        assertEquals(-15.0, bounds.maxY(), 1e-9);
    }

    @Test
    @DisplayName("Расширение увеличивает границы пропорционально фактору")
    void test4() {
        Bounds bounds = new Bounds();

        bounds.update(0.0, 0.0);
        bounds.update(10.0, 20.0);

        bounds.expand(0.2);

        assertEquals(-1.0, bounds.minX(), 1e-9);
        assertEquals(11.0, bounds.maxX(), 1e-9);
        assertEquals(-2.0, bounds.minY(), 1e-9);
        assertEquals(22.0, bounds.maxY(), 1e-9);
    }

    @Test
    @DisplayName("Расширение происходит симметрично относительно центра")
    void test5() {
        Bounds bounds = new Bounds();

        bounds.update(2.0, 4.0);
        bounds.update(6.0, 8.0);

        double centerXBefore = (bounds.minX() + bounds.maxX()) / 2;
        double centerYBefore = (bounds.minY() + bounds.maxY()) / 2;

        bounds.expand(1.0);

        double centerXAfter = (bounds.minX() + bounds.maxX()) / 2;
        double centerYAfter = (bounds.minY() + bounds.maxY()) / 2;

        assertEquals(centerXBefore, centerXAfter, 1e-9);
        assertEquals(centerYBefore, centerYAfter, 1e-9);
    }
}
