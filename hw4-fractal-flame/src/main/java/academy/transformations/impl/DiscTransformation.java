package academy.transformations.impl;

import academy.transformations.Transformation;

public final class DiscTransformation implements Transformation {

    @Override
    public String name() {
        return "disc";
    }

    @Override
    public double[] apply(double x, double y) {

        double r = Math.sqrt(x * x + y * y);
        double theta = Math.atan2(y, x);

        double factor = theta / Math.PI;

        double nx = factor * Math.sin(Math.PI * r);
        double ny = factor * Math.cos(Math.PI * r);

        return new double[] {nx, ny};
    }
}
