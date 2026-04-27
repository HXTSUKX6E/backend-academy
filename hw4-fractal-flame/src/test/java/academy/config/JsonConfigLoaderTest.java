package academy.config;

import static org.junit.jupiter.api.Assertions.*;

import academy.AppConfig;
import academy.affine.AffineTransform;
import academy.transformations.WeightedTransformation;
import academy.transformations.impl.SinusoidalTransformation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JsonConfigLoaderTest {

    @Test
    @DisplayName("Корректно загружается базовая конфигурация из JSON")
    void test1() throws Exception {
        File file = createTempJson(
                        """
            {
              "size": { "width": 800, "height": 600 },
              "iteration_count": 1000,
              "threads": 4,
              "output_path": "out.png",
              "seed": 42,
              "functions": [
                { "name": "sinusoidal", "weight": 1.0 }
              ],
              "affine_params": {
                "a": 1, "b": 0, "c": 0,
                "d": 0, "e": 1, "f": 0
              }
            }
        """)
                .toFile();

        AppConfig config = JsonConfigLoader.load(file.getAbsolutePath());

        assertEquals(800, config.width);
        assertEquals(600, config.height);
        assertEquals(1000L, config.iterationCount);
        assertEquals(4, config.threads);
        assertEquals("out.png", config.outputPath);
        assertEquals(42L, config.seed);
    }

    @Test
    @DisplayName("Seed корректно обрабатывается как double")
    void test2() throws Exception {
        File file = createTempJson(
                        """
            {
              "size": { "width": 100, "height": 100 },
              "iteration_count": 1,
              "threads": 1,
              "output_path": "x.png",
              "seed": 1.5,
              "functions": [],
              "affine_params": {
                "a": 1, "b": 0, "c": 0,
                "d": 0, "e": 1, "f": 0
              }
            }
        """)
                .toFile();

        AppConfig config = JsonConfigLoader.load(file.getAbsolutePath());

        assertEquals(Double.doubleToLongBits(1.5), config.seed);
    }

    @Test
    @DisplayName("Viewport берётся по умолчанию если отсутствует в JSON")
    void test3() throws Exception {
        File file = createTempJson(
                        """
            {
              "size": { "width": 10, "height": 10 },
              "iteration_count": 1,
              "threads": 1,
              "output_path": "x.png",
              "seed": 1,
              "functions": [],
              "affine_params": {
                "a": 1, "b": 0, "c": 0,
                "d": 0, "e": 1, "f": 0
              }
            }
        """)
                .toFile();

        AppConfig config = JsonConfigLoader.load(file.getAbsolutePath());

        assertEquals(-2.0, config.viewportXmin, 1e-9);
        assertEquals(2.0, config.viewportXmax, 1e-9);
        assertEquals(-2.0, config.viewportYmin, 1e-9);
        assertEquals(2.0, config.viewportYmax, 1e-9);
    }

    @Test
    @DisplayName("Корректно парсятся функции с весами и цветами")
    void test4() throws Exception {
        File file = createTempJson(
                        """
            {
              "size": { "width": 10, "height": 10 },
              "iteration_count": 1,
              "threads": 1,
              "output_path": "x.png",
              "seed": 1,
              "functions": [
                {
                  "name": "sinusoidal",
                  "weight": 2.0,
                  "r": 255,
                  "g": 128,
                  "b": 0
                }
              ],
              "affine_params": {
                "a": 1, "b": 0, "c": 0,
                "d": 0, "e": 1, "f": 0
              }
            }
        """)
                .toFile();

        AppConfig config = JsonConfigLoader.load(file.getAbsolutePath());

        assertEquals(1, config.transformations.size());

        WeightedTransformation wt = config.transformations.get(0);

        assertInstanceOf(SinusoidalTransformation.class, wt.transformation());
        assertEquals(2.0, wt.weight(), 1e-9);
        assertEquals(1.0, wt.r(), 1e-9);
        assertEquals(128.0 / 255.0, wt.g(), 1e-9);
        assertEquals(0.0, wt.b(), 1e-9);
    }

    @Test
    @DisplayName("Корректно парсится массив аффинных преобразований")
    void test5() throws Exception {
        File file = createTempJson(
                        """
            {
              "size": { "width": 10, "height": 10 },
              "iteration_count": 1,
              "threads": 1,
              "output_path": "x.png",
              "seed": 1,
              "functions": [],
              "affine_params": [
                { "a": 1, "b": 0, "c": 0, "d": 0, "e": 1, "f": 0 },
                { "a": 0.5, "b": 0, "c": 0, "d": 0, "e": 0.5, "f": 0 }
              ]
            }
        """)
                .toFile();

        AppConfig config = JsonConfigLoader.load(file.getAbsolutePath());

        assertEquals(2, config.affineTransforms.size());

        AffineTransform a1 = config.affineTransforms.get(0);
        AffineTransform a2 = config.affineTransforms.get(1);

        assertEquals(1.0, a1.apply(1, 1)[0], 1e-9);
        assertEquals(0.5, a2.apply(1, 1)[0], 1e-9);
    }

    private Path createTempJson(String content) throws IOException {
        Path file = Files.createTempFile("config", ".json");

        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write(content);
        }

        file.toFile().deleteOnExit();
        return file;
    }
}
