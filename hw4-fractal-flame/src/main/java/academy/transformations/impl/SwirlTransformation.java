package academy.transformations.impl;

import academy.transformations.Transformation;

public final class SwirlTransformation implements Transformation {

    @Override
    public String name() {
        return "swirl";
    }

    @Override
    public double[] apply(double x, double y) {
        double r2 = x * x + y * y;
        return new double[] {x * Math.sin(r2) - y * Math.cos(r2), x * Math.cos(r2) + y * Math.sin(r2)};
    }
}
