package academy.render;

import static org.junit.jupiter.api.Assertions.*;

import academy.AppConfig;
import academy.core.Histogram;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FlameRendererTest {

    @Test
    @DisplayName("Рендер возвращает изображение нужного размера")
    void test1() {
        Histogram histogram = new Histogram(10, 8);
        AppConfig config = new AppConfig();

        FlameRenderer renderer = new FlameRenderer();
        BufferedImage image = renderer.render(config, histogram);

        assertNotNull(image);
        assertEquals(10, image.getWidth());
        assertEquals(8, image.getHeight());
    }

    @Test
    @DisplayName("При нулевой плотности возвращается чёрное изображение")
    void test2() {
        Histogram histogram = new Histogram(5, 5);
        AppConfig config = new AppConfig();

        FlameRenderer renderer = new FlameRenderer();
        BufferedImage image = renderer.render(config, histogram);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y) & 0x00FFFFFF;
                assertEquals(0x000000, rgb);
            }
        }
    }

    @Test
    @DisplayName("Ненулевая плотность приводит к появлению ненулевых пикселей")
    void test3() {
        Histogram histogram = new Histogram(5, 5);
        histogram.hit(2, 2, 1.0, 0.5, 0.25);

        AppConfig config = new AppConfig();
        config.brightness = 3.0;

        FlameRenderer renderer = new FlameRenderer();
        BufferedImage image = renderer.render(config, histogram);

        int rgb = image.getRGB(2, 2);
        assertNotEquals(0x000000, rgb);
    }

    @Test
    @DisplayName("Яркость влияет на итоговую интенсивность цвета")
    void test4() {
        Histogram histogram = new Histogram(3, 3);

        histogram.hit(1, 1, 0.1, 0.1, 0.1);

        AppConfig c1 = new AppConfig();
        c1.brightness = 0.5;

        AppConfig c2 = new AppConfig();
        c2.brightness = 2.0;

        FlameRenderer renderer = new FlameRenderer();

        BufferedImage img1 = renderer.render(c1, histogram);
        BufferedImage img2 = renderer.render(c2, histogram);

        int rgb1 = img1.getRGB(1, 1) & 0x00FFFFFF;
        int rgb2 = img2.getRGB(1, 1) & 0x00FFFFFF;

        assertNotEquals(rgb1, rgb2);
        assertTrue(rgb2 > rgb1);
    }

    @Test
    @DisplayName("Gamma correction влияет на итоговый цвет")
    void test5() {
        Histogram histogram = new Histogram(3, 3);
        histogram.hit(1, 1, 1.0, 0.5, 0.25);

        AppConfig config = new AppConfig();
        config.gammaCorrection = true;
        config.gamma = 2.2;
        config.brightness = 3.0;

        FlameRenderer renderer = new FlameRenderer();
        BufferedImage image = renderer.render(config, histogram);

        int rgb = image.getRGB(1, 1);
        assertNotEquals(0x000000, rgb);
    }

    @Test
    @DisplayName("Рендер не выбрасывает исключения при корректных входных данных")
    void test6() {
        Histogram histogram = new Histogram(50, 50);
        histogram.hit(10, 10, 0.3, 0.4, 0.5);
        histogram.hit(20, 30, 0.6, 0.2, 0.1);

        AppConfig config = new AppConfig();
        config.gammaCorrection = true;
        config.gamma = 2.0;
        config.brightness = 2.5;

        FlameRenderer renderer = new FlameRenderer();

        assertDoesNotThrow(() -> renderer.render(config, histogram));
    }
}
