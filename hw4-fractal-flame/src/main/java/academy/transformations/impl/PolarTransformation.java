package academy.transformations.impl;

import academy.transformations.Transformation;

public final class PolarTransformation implements Transformation {

    @Override
    public String name() {
        return "polar";
    }

    @Override
    public double[] apply(double x, double y) {

        double r = Math.sqrt(x * x + y * y);
        double theta = Math.atan2(y, x);

        double nx = theta / Math.PI;
        double ny = r - 1.0;

        return new double[] {nx, ny};
    }
}
