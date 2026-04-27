package academy.transformations.impl;

import academy.transformations.Transformation;

public final class SinusoidalTransformation implements Transformation {

    @Override
    public String name() {
        return "sinusoidal";
    }

    @Override
    public double[] apply(double x, double y) {
        return new double[] {Math.sin(x), Math.sin(y)};
    }
}
