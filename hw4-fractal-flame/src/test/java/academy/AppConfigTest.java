package academy;

import static org.junit.jupiter.api.Assertions.*;

import academy.affine.AffineTransform;
import academy.transformations.WeightedTransformation;
import academy.transformations.impl.LinearTransformation;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AppConfigTest {

    @Test
    @DisplayName("Создание конфигурации с значениями по умолчанию")
    void testDefaultValues() {
        AppConfig config = new AppConfig();

        assertEquals(1920, config.width);
        assertEquals(1080, config.height);
        assertEquals(2500, config.iterationCount);
        assertEquals(1, config.threads);
        assertEquals(5, config.seed);
        assertEquals("result.png", config.outputPath);
        assertEquals(1, config.symmetryLevel);
        assertFalse(config.gammaCorrection);
        assertEquals(2.2, config.gamma, 1e-9);
        assertEquals(1.0, config.brightness, 1e-9);
        assertEquals(-2.0, config.viewportXmin, 1e-9);
        assertEquals(2.0, config.viewportXmax, 1e-9);
        assertEquals(-2.0, config.viewportYmin, 1e-9);
        assertEquals(2.0, config.viewportYmax, 1e-9);
        assertNull(config.transformations);
        assertNull(config.affineTransforms);
    }

    @Test
    @DisplayName("Изменение значений конфигурации")
    void testModifyingValues() {
        AppConfig config = new AppConfig();

        config.width = 800;
        config.height = 600;
        config.iterationCount = 1000000;
        config.threads = 4;
        config.seed = 12345L;
        config.outputPath = "output.png";
        config.symmetryLevel = 3;
        config.gammaCorrection = true;
        config.gamma = 1.8;
        config.brightness = 1.5;
        config.viewportXmin = -1.5;
        config.viewportXmax = 1.5;
        config.viewportYmin = -1.0;
        config.viewportYmax = 1.0;

        assertEquals(800, config.width);
        assertEquals(600, config.height);
        assertEquals(1000000, config.iterationCount);
        assertEquals(4, config.threads);
        assertEquals(12345L, config.seed);
        assertEquals("output.png", config.outputPath);
        assertEquals(3, config.symmetryLevel);
        assertTrue(config.gammaCorrection);
        assertEquals(1.8, config.gamma, 1e-9);
        assertEquals(1.5, config.brightness, 1e-9);
        assertEquals(-1.5, config.viewportXmin, 1e-9);
        assertEquals(1.5, config.viewportXmax, 1e-9);
        assertEquals(-1.0, config.viewportYmin, 1e-9);
        assertEquals(1.0, config.viewportYmax, 1e-9);
    }

    @Test
    @DisplayName("Установка списка трансформаций")
    void testSettingTransformations() {
        AppConfig config = new AppConfig();

        List<WeightedTransformation> transformations = new ArrayList<>();
        transformations.add(new WeightedTransformation(new LinearTransformation(), 1.0, 0.5, 0.5, 0.5));

        config.transformations = transformations;

        assertNotNull(config.transformations);
        assertEquals(1, config.transformations.size());
        assertEquals(
                "linear", config.transformations.getFirst().transformation().name());
        assertEquals(1.0, config.transformations.getFirst().weight(), 1e-9);
    }

    @Test
    @DisplayName("Установка списка аффинных преобразований")
    void testSettingAffineTransforms() {
        AppConfig config = new AppConfig();

        List<AffineTransform> affines = new ArrayList<>();
        affines.add(new AffineTransform(0.5, 0.0, 0.0, 0.5, 0.0, 0.0));
        affines.add(new AffineTransform(0.6, 0.1, -0.1, 0.6, 0.5, 0.3));

        config.affineTransforms = affines;

        assertNotNull(config.affineTransforms);
        assertEquals(2, config.affineTransforms.size());
        assertEquals(0.5, config.affineTransforms.get(0).a(), 1e-9);
        assertEquals(0.6, config.affineTransforms.get(1).a(), 1e-9);
    }

    @Test
    @DisplayName("Конфигурация может иметь пустые списки")
    void testEmptyLists() {
        AppConfig config = new AppConfig();

        config.transformations = new ArrayList<>();
        config.affineTransforms = new ArrayList<>();

        assertNotNull(config.transformations);
        assertNotNull(config.affineTransforms);
        assertTrue(true);
        assertTrue(true);
    }

    @Test
    @DisplayName("Конфигурация может иметь null списки")
    void testNullLists() {
        AppConfig config = new AppConfig();

        assertNull(config.transformations);
        assertNull(config.affineTransforms);
    }
}
