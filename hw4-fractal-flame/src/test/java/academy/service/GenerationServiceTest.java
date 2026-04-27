package academy.service;

import static org.junit.jupiter.api.Assertions.*;

import academy.AppConfig;
import academy.affine.AffineTransform;
import academy.transformations.WeightedTransformation;
import academy.transformations.impl.LinearTransformation;
import academy.transformations.impl.SinusoidalTransformation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GenerationServiceTest {

    @Test
    @DisplayName("Генерация корректно работает в однопоточном режиме")
    void test1() throws Exception {
        Path tempDir = Files.createTempDirectory("gen-test");
        Path output = tempDir.resolve("single.png");

        AppConfig config = new AppConfig();
        config.width = 50;
        config.height = 50;
        config.iterationCount = 100;
        config.threads = 1;
        config.seed = 42L;
        config.outputPath = output.toString();

        config.viewportXmin = -2.0;
        config.viewportXmax = 2.0;
        config.viewportYmin = -2.0;
        config.viewportYmax = 2.0;

        config.affineTransforms = List.of(new AffineTransform(0.5, 0.0, 0.0, 0.0, 0.5, 0.0));

        config.transformations =
                List.of(new WeightedTransformation(new SinusoidalTransformation(), 1.0, 1.0, 1.0, 1.0));

        GenerationService service = new GenerationService();
        service.generate(config);

        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);
    }

    @Test
    @DisplayName("Генерация работает с минимальной конфигурацией")
    void test2() throws Exception {
        Path output = Files.createTempFile("test-output", ".png");

        AppConfig config = new AppConfig();
        config.width = 50;
        config.height = 50;
        config.iterationCount = 100;
        config.threads = 4;
        config.symmetryLevel = 1;
        config.outputPath = output.toString();

        config.transformations = new ArrayList<>();
        config.affineTransforms = new ArrayList<>();

        config.transformations.add(new WeightedTransformation(new LinearTransformation(), 1.0, 0.5, 0.5, 0.5));

        config.affineTransforms.add(new AffineTransform(0.5, 0.0, 0.0, 0.5, 0.0, 0.0));

        GenerationService service = new GenerationService();

        assertDoesNotThrow(() -> service.generate(config));

        if (Files.exists(output)) {
            Files.delete(output);
        }
    }

    @Test
    @DisplayName("Ошибка записи изображения приводит к RuntimeException")
    void test3() {
        AppConfig config = new AppConfig();
        config.width = 10;
        config.height = 10;
        config.iterationCount = 10;
        config.threads = 1;

        // гарантированно некорректный путь
        config.outputPath = "/";

        GenerationService service = new GenerationService();

        assertThrows(RuntimeException.class, () -> service.generate(config));
    }
}
