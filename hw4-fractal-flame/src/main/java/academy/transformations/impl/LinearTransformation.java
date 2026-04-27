package academy.transformations.impl;

import academy.transformations.Transformation;

public final class LinearTransformation implements Transformation {

    @Override
    public String name() {
        return "linear";
    }

    @Override
    public double[] apply(double x, double y) {
        return new double[] {x, y};
    }
}
