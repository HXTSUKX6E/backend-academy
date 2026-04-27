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

class MultiThreadExecutorTest {

    private AppConfig baseConfig() {
        AppConfig c = new AppConfig();

        c.width = 100;
        c.height = 100;
        c.iterationCount = 10_000;
        c.seed = 123L;

        c.viewportXmin = -2.0;
        c.viewportXmax = 2.0;
        c.viewportYmin = -2.0;
        c.viewportYmax = 2.0;

        c.affineTransforms = List.of(new AffineTransform(0.5, 0, 0, 0, 0.5, 0));

        c.transformations = List.of(new WeightedTransformation(new SinusoidalTransformation(), 1.0, 1.0, 1.0, 1.0));

        return c;
    }

    @Test
    @DisplayName("Многопоточный исполнитель возвращает непустую гистограмму")
    void test1() {
        AppConfig c = baseConfig();
        c.threads = 4;

        Histogram h = MultiThreadExecutor.execute(c);

        assertNotNull(h);
        assertTrue(h.maxDensity() > 0.0);
    }

    @Test
    @DisplayName("Исполнитель корректно работает при одном потоке")
    void test2() {
        AppConfig c = baseConfig();
        c.threads = 1;

        Histogram h = MultiThreadExecutor.execute(c);

        assertNotNull(h);
        assertTrue(h.maxDensity() > 0.0);
    }

    @Test
    @DisplayName("Результат детерминирован при фиксированном seed")
    void test3() {
        AppConfig c = baseConfig();
        c.threads = 3;

        Histogram h1 = MultiThreadExecutor.execute(c);
        Histogram h2 = MultiThreadExecutor.execute(c);

        assertEquals(h1.maxDensity(), h2.maxDensity(), 1e-9);
    }

    @Test
    @DisplayName("Исполнитель не выбрасывает исключений при корректной конфигурации")
    void test4() {
        AppConfig c = baseConfig();
        c.threads = 8;

        assertDoesNotThrow(() -> MultiThreadExecutor.execute(c));
    }

    @Test
    @DisplayName("Количество потоков меньше 1 обрабатывается корректно")
    void test5() {
        AppConfig c = baseConfig();
        c.threads = 0;

        Histogram h = MultiThreadExecutor.execute(c);

        assertNotNull(h);
        assertTrue(h.maxDensity() > 0.0);
    }
}
