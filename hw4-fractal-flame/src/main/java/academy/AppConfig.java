package academy;

import academy.affine.AffineTransform;
import academy.transformations.WeightedTransformation;
import java.util.List;

public final class AppConfig {

    public int width = 1920;
    public int height = 1080;

    public long iterationCount = 2500;
    public int threads = 1;

    public long seed = 5;

    public String outputPath = "result.png";

    public List<WeightedTransformation> transformations;
    public List<AffineTransform> affineTransforms;

    public int symmetryLevel = 1;

    public boolean gammaCorrection = false;
    public double gamma = 2.2;

    public double brightness = 1.0;

    public double viewportXmin = -2.0;
    public double viewportXmax = 2.0;
    public double viewportYmin = -2.0;
    public double viewportYmax = 2.0;
}
