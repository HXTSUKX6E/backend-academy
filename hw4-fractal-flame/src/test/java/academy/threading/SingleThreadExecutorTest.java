package academy.threading;

import static org.junit.jupiter.api.Assertions.*;

import academy.AppConfig;
import academy.affine.AffineTransform;
import academy.core.Histogram;
import academy.transformations.WeightedTransformation;
import academy.transformations.impl.SinusoidalTransformation;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SingleThreadExecutorTest {

    private AppConfig baseConfig() {
        AppConfig c = new AppConfig();

        c.width = 100;
        c.height = 100;
        c.iterationCount = 10_000;
        c.seed = 42L;

        c.viewportXmin = -2.0;
        c.viewportXmax = 2.0;
        c.viewportYmin = -2.0;
        c.viewportYmax = 2.0;

        c.affineTransforms = List.of(new AffineTransform(0.5, 0, 0, 0, 0.5, 0));

        c.transformations = List.of(new WeightedTransformation(new SinusoidalTransformation(), 1.0, 1.0, 1.0, 1.0));

        return c;
    }

    @Test
    @DisplayName("Однопоточный исполнитель возвращает гистограмму корректного размера")
    void test1() {
        AppConfig c = baseConfig();

        Histogram h = SingleThreadExecutor.execute(c);

        assertNotNull(h);
        assertEquals(c.width, h.width);
        assertEquals(c.height, h.height);
    }

    @Test
    @DisplayName("Однопоточный исполнитель заполняет гистограмму при ненулевых итерациях")
    void test2() {
        AppConfig c = baseConfig();

        Histogram h = SingleThreadExecutor.execute(c);

        assertTrue(h.maxDensity() > 0.0);
    }

    @Test
    @DisplayName("Однопоточный исполнитель корректно обрабатывает нулевое число итераций")
    void test3() {
        AppConfig c = baseConfig();
        c.iterationCount = 0;

        Histogram h = SingleThreadExecutor.execute(c);

        assertEquals(0.0, h.maxDensity(), 1e-9);
    }

    @Test
    @DisplayName("Однопоточный исполнитель детерминирован при фиксированном seed")
    void test4() {
        AppConfig c = baseConfig();

        Histogram h1 = SingleThreadExecutor.execute(c);
        Histogram h2 = SingleThreadExecutor.execute(c);

        assertEquals(h1.maxDensity(), h2.maxDensity(), 1e-9);
    }

    @Test
    @DisplayName("Однопоточный исполнитель не выбрасывает исключений")
    void test5() {
        AppConfig c = baseConfig();

        assertDoesNotThrow(() -> SingleThreadExecutor.execute(c));
    }
}
