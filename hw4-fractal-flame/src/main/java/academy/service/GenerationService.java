package academy.service;

import academy.AppConfig;
import academy.core.Histogram;
import academy.render.FlameRenderer;
import academy.threading.MultiThreadExecutor;
import academy.threading.SingleThreadExecutor;
import java.nio.file.Path;
import javax.imageio.ImageIO;

public final class GenerationService {

    public void generate(AppConfig config) {

        Histogram histogram;

        if (config.threads <= 1) {
            histogram = SingleThreadExecutor.execute(config);
        } else {
            histogram = MultiThreadExecutor.execute(config);
        }

        FlameRenderer renderer = new FlameRenderer();
        var image = renderer.render(config, histogram);

        try {
            ImageIO.write(image, "PNG", Path.of(config.outputPath).toFile());
        } catch (Exception e) {
            throw new RuntimeException("Failed to write output image", e);
        }
    }
}
