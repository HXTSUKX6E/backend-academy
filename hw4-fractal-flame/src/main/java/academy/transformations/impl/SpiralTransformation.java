package academy.transformations.impl;

import academy.transformations.Transformation;

public final class SpiralTransformation implements Transformation {

    @Override
    public String name() {
        return "spiral";
    }

    @Override
    public double[] apply(double x, double y) {

        double r = Math.sqrt(x * x + y * y) + 1e-6;
        double theta = Math.atan2(y, x);

        double nx = (Math.cos(theta) + Math.sin(r)) / r;
        double ny = (Math.sin(theta) - Math.cos(r)) / r;

        return new double[] {nx, ny};
    }
}
