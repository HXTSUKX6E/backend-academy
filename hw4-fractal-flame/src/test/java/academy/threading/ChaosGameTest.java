package academy.threading;

import static org.junit.jupiter.api.Assertions.*;

import academy.AppConfig;
import academy.affine.AffineTransform;
import academy.core.Bounds;
import academy.core.Histogram;
import academy.transformations.WeightedTransformation;
import academy.transformations.impl.SinusoidalTransformation;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChaosGameTest {

    private AppConfig baseConfig() {
        AppConfig c = new AppConfig();

        c.width = 100;
        c.height = 100;
        c.seed = 123L;
        c.symmetryLevel = 1;

        c.viewportXmin = -2.0;
        c.viewportXmax = 2.0;
        c.viewportYmin = -2.0;
        c.viewportYmax = 2.0;

        c.affineTransforms = List.of(new AffineTransform(0.5, 0, 0, 0, 0.5, 0));

        c.transformations = List.of(new WeightedTransformation(new SinusoidalTransformation(), 1.0, 1.0, 1.0, 1.0));

        return c;
    }

    @Test
    @DisplayName("computeBounds возвращает валидные границы")
    void test1() {
        AppConfig c = baseConfig();

        Bounds bounds = ChaosGame.computeBounds(c, 10_000, 1);

        assertTrue(bounds.minX() < bounds.maxX());
        assertTrue(bounds.minY() < bounds.maxY());

        assertTrue(Double.isFinite(bounds.minX()));
        assertTrue(Double.isFinite(bounds.maxX()));
        assertTrue(Double.isFinite(bounds.minY()));
        assertTrue(Double.isFinite(bounds.maxY()));
    }

    @Test
    @DisplayName("computeBounds детерминирован при фиксированном seed")
    void test2() {
        AppConfig c = baseConfig();

        Bounds b1 = ChaosGame.computeBounds(c, 5_000, 1);
        Bounds b2 = ChaosGame.computeBounds(c, 5_000, 1);

        assertEquals(b1.minX(), b2.minX(), 1e-9);
        assertEquals(b1.maxX(), b2.maxX(), 1e-9);
        assertEquals(b1.minY(), b2.minY(), 1e-9);
        assertEquals(b1.maxY(), b2.maxY(), 1e-9);
    }

    @Test
    @DisplayName("run записывает данные в гистограмму")
    void test3() {
        AppConfig c = baseConfig();
        Histogram h = new Histogram(c.width, c.height);

        ChaosGame.run(c, h, 5_000, 0);

        assertTrue(h.maxDensity() > 0.0);
    }

    @Test
    @DisplayName("run корректно работает при симметрии больше 1")
    void test4() {
        AppConfig c = baseConfig();
        c.symmetryLevel = 4;

        Histogram h = new Histogram(c.width, c.height);

        assertDoesNotThrow(() -> ChaosGame.run(c, h, 3_000, 0));

        assertTrue(h.maxDensity() > 0.0);
    }

    @Test
    @DisplayName("run корректно обрабатывает нулевое число итераций")
    void test5() {
        AppConfig c = baseConfig();
        Histogram h = new Histogram(c.width, c.height);

        ChaosGame.run(c, h, 0, 0);

        assertEquals(0.0, h.maxDensity(), 1e-9);
    }
}
