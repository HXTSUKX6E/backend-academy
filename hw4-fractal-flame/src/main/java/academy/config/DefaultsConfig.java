package academy.config;

import academy.AppConfig;
import academy.affine.AffineTransform;
import academy.transformations.WeightedTransformation;
import academy.transformations.impl.SinusoidalTransformation;
import java.util.ArrayList;

public final class DefaultsConfig {

    private DefaultsConfig() {}

    public static AppConfig create() {

        AppConfig config = new AppConfig();

        config.width = 1920;
        config.height = 1080;
        config.iterationCount = 2500;
        config.threads = 1;
        config.seed = 5L;
        config.outputPath = "result.png";

        config.gammaCorrection = false;
        config.gamma = 2.2;
        config.symmetryLevel = 1;

        config.affineTransforms = new ArrayList<>();
        config.transformations = new ArrayList<>();

        config.affineTransforms.add(new AffineTransform(0.5, 0.0, 0.0, 0.0, 0.5, 0.0));

        config.transformations.add(new WeightedTransformation(new SinusoidalTransformation(), 1.0, 1.0, 1.0, 1.0));

        return config;
    }
}
