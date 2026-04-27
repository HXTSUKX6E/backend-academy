package academy.threading;

import academy.AppConfig;
import academy.core.Histogram;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MultiThreadExecutor {

    private static final Logger logger = LogManager.getLogger(MultiThreadExecutor.class);

    private MultiThreadExecutor() {}

    public static Histogram execute(AppConfig config) {
        int threads = Math.max(1, config.threads);

        logger.info("Starting fractal generation");
        logger.info(
                "Configuration: {}x{} image, {} iterations, {} thread(s), symmetry level {}",
                config.width,
                config.height,
                config.iterationCount,
                threads,
                config.symmetryLevel);

        Histogram sharedHistogram = new Histogram(config.width, config.height);

        Thread[] workers = new Thread[threads];
        long totalIterations = config.iterationCount;
        long base = totalIterations / threads;
        long remainder = totalIterations % threads;

        ThreadLocal<Histogram> threadLocalBuffer =
                ThreadLocal.withInitial(() -> new Histogram(config.width, config.height));

        long startTime = System.currentTimeMillis();
        logger.info("Starting {} worker threads", threads);

        for (int i = 0; i < threads; i++) {
            final int threadIndex = i;
            final long iters = base + (i < remainder ? 1 : 0);

            Thread t = new Thread(() -> {
                try {
                    Histogram localBuffer = threadLocalBuffer.get();
                    ChaosGame.run(config, localBuffer, iters, threadIndex);

                    synchronized (sharedHistogram) {
                        sharedHistogram.addFrom(localBuffer);
                    }
                } catch (Exception e) {
                    logger.error("Thread {} failed: {}", threadIndex, e.getMessage(), e);
                    throw e;
                }
            });

            workers[i] = t;
            t.start();
        }

        for (Thread t : workers) {
            try {
                t.join();
            } catch (InterruptedException e) {
                logger.error("Thread interrupted: {}", e.getMessage(), e);
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted", e);
            }
        }

        threadLocalBuffer.remove();

        long endTime = System.currentTimeMillis();
        double elapsedSeconds = (endTime - startTime) / 1000.0;

        logger.info("All threads completed in {} seconds", String.format("%.2f", elapsedSeconds));
        logger.info("Histogram max density: {}", sharedHistogram.maxDensity());

        return sharedHistogram;
    }
}
