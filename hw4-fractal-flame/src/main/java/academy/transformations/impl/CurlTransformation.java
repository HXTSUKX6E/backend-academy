package academy.transformations.impl;

import academy.transformations.Transformation;

public final class CurlTransformation implements Transformation {

    private final double c1;
    private final double c2;

    public CurlTransformation() {
        this(0.5, 0.2);
    }

    public CurlTransformation(double c1, double c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    @Override
    public String name() {
        return "curl";
    }

    @Override
    public double[] apply(double x, double y) {
        double r2 = x * x + y * y;
        double t = 1.0 + c1 * x + c2 * (x * x - y * y);

        double nx = (x + c1 * r2) / t;
        double ny = (y + c2 * r2) / t;

        return new double[] {nx, ny};
    }
}
