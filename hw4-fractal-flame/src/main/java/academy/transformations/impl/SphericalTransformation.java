package academy.transformations.impl;

import academy.transformations.Transformation;

public final class SphericalTransformation implements Transformation {

    @Override
    public String name() {
        return "spherical";
    }

    @Override
    public double[] apply(double x, double y) {
        double r2 = x * x + y * y + 1e-6;
        return new double[] {x / r2, y / r2};
    }
}
