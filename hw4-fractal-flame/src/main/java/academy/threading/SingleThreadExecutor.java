package academy.threading;

import academy.AppConfig;
import academy.core.Histogram;

public final class SingleThreadExecutor {

    private SingleThreadExecutor() {}

    public static Histogram execute(AppConfig config) {

        Histogram h = new Histogram(config.width, config.height);

        ChaosGame.run(config, h, config.iterationCount, 0);

        return h;
    }
}
