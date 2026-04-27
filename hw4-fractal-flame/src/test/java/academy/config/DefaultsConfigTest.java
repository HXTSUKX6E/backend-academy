package academy.config;

import static org.junit.jupiter.api.Assertions.*;

import academy.AppConfig;
import academy.affine.AffineTransform;
import academy.transformations.WeightedTransformation;
import academy.transformations.impl.SinusoidalTransformation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultsConfigTest {

    @Test
    @DisplayName("Создаётся конфигурация со значениями по умолчанию")
    void test1() {
        AppConfig config = DefaultsConfig.create();

        assertEquals(1920, config.width);
        assertEquals(1080, config.height);
        assertEquals(2500, config.iterationCount);
        assertEquals(1, config.threads);
        assertEquals(5L, config.seed);
        assertEquals("result.png", config.outputPath);

        assertFalse(config.gammaCorrection);
        assertEquals(2.2, config.gamma, 1e-9);
        assertEquals(1, config.symmetryLevel);
    }

    @Test
    @DisplayName("Списки аффинных преобразований и трансформаций инициализированы")
    void test2() {
        AppConfig config = DefaultsConfig.create();

        assertNotNull(config.affineTransforms);
        assertNotNull(config.transformations);

        assertEquals(1, config.affineTransforms.size());
        assertEquals(1, config.transformations.size());
    }

    @Test
    @DisplayName("Аффинное преобразование по умолчанию задано корректно")
    void test3() {
        AppConfig config = DefaultsConfig.create();

        AffineTransform affine = config.affineTransforms.get(0);

        double[] result = affine.apply(2.0, 4.0);

        assertEquals(1.0, result[0], 1e-9);
        assertEquals(2.0, result[1], 1e-9);
    }

    @Test
    @DisplayName("Трансформация по умолчанию является синусоидальной с весом 1.0")
    void test4() {
        AppConfig config = DefaultsConfig.create();

        assertNotNull(config.transformations.getFirst());

        WeightedTransformation wt = config.transformations.getFirst();

        assertInstanceOf(SinusoidalTransformation.class, wt.transformation());
        assertEquals(1.0, wt.weight(), 1e-9);
    }

    @Test
    @DisplayName("Каждый вызов create возвращает независимую конфигурацию")
    void test5() {
        AppConfig c1 = DefaultsConfig.create();
        AppConfig c2 = DefaultsConfig.create();

        c1.width = 800;
        c1.affineTransforms.clear();

        assertNotEquals(c1.width, c2.width);
        assertNotEquals(c1.affineTransforms.size(), c2.affineTransforms.size());
    }
}
