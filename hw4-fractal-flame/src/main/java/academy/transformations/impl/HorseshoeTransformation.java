package academy.transformations.impl;

import academy.transformations.Transformation;

public final class HorseshoeTransformation implements Transformation {

    @Override
    public String name() {
        return "horseshoe";
    }

    @Override
    public double[] apply(double x, double y) {
        double r = Math.sqrt(x * x + y * y) + 1e-6;
        return new double[] {(x - y) * (x + y) / r, 2 * x * y / r};
    }
}
