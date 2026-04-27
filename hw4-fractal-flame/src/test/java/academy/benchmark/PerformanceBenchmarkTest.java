package academy.benchmark;

import academy.AppConfig;
import academy.affine.AffineTransform;
import academy.service.GenerationService;
import academy.transformations.WeightedTransformation;
import academy.transformations.impl.LinearTransformation;
import academy.transformations.impl.SinusoidalTransformation;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PerformanceBenchmarkTest {

    private AppConfig benchmarkConfig(int threads) {
        AppConfig c = new AppConfig();

        c.width = 1200;
        c.height = 1200;

        c.iterationCount = 5_000_000;

        c.threads = threads;
        c.seed = 91;
        c.outputPath = "benchmark_" + threads + ".png";

        c.viewportXmin = -1.2;
        c.viewportXmax = 1.2;
        c.viewportYmin = -1.2;
        c.viewportYmax = 1.2;

        c.symmetryLevel = 2;
        c.gammaCorrection = true;
        c.gamma = 2.2;

        c.transformations = List.of(
                new WeightedTransformation(new LinearTransformation(), 0.01, 0.2, 1.0, 0.2),
                new WeightedTransformation(new SinusoidalTransformation(), 0.2, 0.2, 0.2, 1.0));

        c.affineTransforms = List.of(
                new AffineTransform(0.7, -0.4, 0.0, 0.4, 0.7, 0.0),
                new AffineTransform(0.5, 0.3, 0.0, -0.3, 0.5, 0.5),
                new AffineTransform(0.6, -0.2, 0.0, 0.2, 0.6, -0.5));

        return c;
    }

    @Test
    @DisplayName("Benchmark: 1 / 2 / 4 / 8 потоков")
    void test1() {
        int[] threads = {1, 2, 4, 8};

        for (int t : threads) {
            AppConfig config = benchmarkConfig(t);
            GenerationService service = new GenerationService();

            long start = System.nanoTime();
            service.generate(config);
            long end = System.nanoTime();

            double seconds = (end - start) / 1_000_000_000.0;

            System.out.printf("Threads: %d | Time: %.3f sec%n", t, seconds);
        }
    }
}
