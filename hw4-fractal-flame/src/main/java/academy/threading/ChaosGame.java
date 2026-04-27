package academy.threading;

import academy.AppConfig;
import academy.affine.AffineTransform;
import academy.core.Bounds;
import academy.core.Histogram;
import academy.transformations.WeightedTransformation;
import academy.utils.RandomUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ChaosGame {

    private static final Logger logger = LogManager.getLogger(ChaosGame.class);

    private ChaosGame() {}

    private static final Random rnd = new Random();

    public static Bounds computeBounds(AppConfig c, long iterations, int threads) {
        logger.info("Computing bounds for {} threads", threads);

        Bounds bounds = new Bounds();
        bounds.update(-2.0, -2.0);
        bounds.update(2.0, 2.0);
        bounds.update(-2.0, 2.0);
        bounds.update(2.0, -2.0);

        List<WeightedTransformation> funcs = c.transformations;
        List<AffineTransform> affines = c.affineTransforms;

        int burnIn = 1000;
        int samplePoints = (int) Math.min(iterations, 100_000L);

        for (int t = 0; t < Math.max(1, threads); t++) {
            rnd.setSeed(c.seed + t * 997L);

            double x = rnd.nextDouble() * 4.0 - 2.0;
            double y = rnd.nextDouble() * 4.0 - 2.0;

            for (int i = 0; i < samplePoints + burnIn; i++) {
                WeightedTransformation wt = RandomUtils.weighted(funcs, rnd);
                AffineTransform a = affines.get(rnd.nextInt(affines.size()));

                double[] aff = a.apply(x, y);
                double[] v = wt.transformation().apply(aff[0], aff[1]);

                if (!Double.isFinite(v[0]) || !Double.isFinite(v[1])) {
                    continue;
                }

                x = v[0];
                y = v[1];

                if (i >= burnIn) {
                    bounds.update(x, y);
                }
            }
        }

        double width = bounds.maxX() - bounds.minX();
        double height = bounds.maxY() - bounds.minY();

        if (width > 100 || height > 100 || width < 0.1 || height < 0.1) {
            logger.warn("Unrealistic bounds [{} x {}], using default [-2, 2]", width, height);
            bounds.reset(-2.0, 2.0, -2.0, 2.0);
        }

        bounds.expand(0.1);

        logger.info(
                "Computed bounds: [{}, {}] x [{}, {}] (size: {} x {})",
                String.format("%.3f", bounds.minX()),
                String.format("%.3f", bounds.maxX()),
                String.format("%.3f", bounds.minY()),
                String.format("%.3f", bounds.maxY()),
                String.format("%.3f", bounds.maxX() - bounds.minX()),
                String.format("%.3f", bounds.maxY() - bounds.minY()));

        return bounds;
    }

    @SuppressFBWarnings(
            value = "DMI_RANDOM_USED_ONLY_ONCE",
            justification = "Seed-based deterministic random is required")
    public static void run(AppConfig c, Histogram h, long iterations, int threadIndex) {
        logger.info("Thread {} started (iterations: {})", threadIndex, iterations);

        Random rnd = new Random(c.seed + threadIndex * 31L);

        double x = rnd.nextDouble() * 4.0 - 2.0;
        double y = rnd.nextDouble() * 4.0 - 2.0;

        double r = rnd.nextDouble();
        double g = rnd.nextDouble();
        double b = rnd.nextDouble();

        int burnIn = 100000;
        List<WeightedTransformation> funcs = c.transformations;
        List<AffineTransform> affines = c.affineTransforms;

        double minX = c.viewportXmin;
        double maxX = c.viewportXmax;
        double minY = c.viewportYmin;
        double maxY = c.viewportYmax;

        double dx = maxX - minX;
        double dy = maxY - minY;

        int symmetry = Math.max(1, c.symmetryLevel);

        long lastProgressTime = System.currentTimeMillis();
        long progressInterval = 10000; // Каждые 10 секунд
        long lastProgressPercent = 0;

        for (long i = 0; i < iterations + burnIn; i++) {
            WeightedTransformation wt = RandomUtils.weighted(funcs, rnd);
            AffineTransform a = affines.get(rnd.nextInt(affines.size()));

            double[] aff = a.apply(x, y);
            double[] v = wt.transformation().apply(aff[0], aff[1]);

            if (!Double.isFinite(v[0]) || !Double.isFinite(v[1])) {
                x = rnd.nextDouble() * 4.0 - 2.0;
                y = rnd.nextDouble() * 4.0 - 2.0;
                continue;
            }

            x = v[0];
            y = v[1];

            double cm = 0.3;
            r = r * (1.0 - cm) + wt.r() * cm;
            g = g * (1.0 - cm) + wt.g() * cm;
            b = b * (1.0 - cm) + wt.b() * cm;

            if (i < burnIn) {
                continue;
            }

            long currentIteration = i - burnIn;

            long currentTime = System.currentTimeMillis();
            long currentPercent = currentIteration * 100 / iterations;

            if (currentTime - lastProgressTime >= progressInterval || currentPercent >= lastProgressPercent + 10) {

                logger.info(
                        "Thread {}: {}% ({} / {} iterations)",
                        threadIndex, currentPercent, currentIteration, iterations);

                lastProgressTime = currentTime;
                lastProgressPercent = currentPercent;
            }

            for (int k = 0; k < symmetry; k++) {
                double angle = 2.0 * Math.PI * k / symmetry;
                double xs = x * Math.cos(angle) - y * Math.sin(angle);
                double ys = x * Math.sin(angle) + y * Math.cos(angle);

                double normalizedX = (xs - minX) / dx;
                double normalizedY = (ys - minY) / dy;

                normalizedX = Math.max(0.0, Math.min(1.0, normalizedX));
                normalizedY = Math.max(0.0, Math.min(1.0, normalizedY));

                int px = (int) (normalizedX * (c.width - 1));
                int py = (int) (normalizedY * (c.height - 1));

                if (px < 0 || px >= c.width || py < 0 || py >= c.height) {
                    continue;
                }

                h.hit(px, py, r, g, b);
            }
        }

        logger.info("Thread {} finished", threadIndex);
    }
}
