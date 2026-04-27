package academy.affine;

public record AffineTransform(double a, double b, double c, double d, double e, double f) {
    public double[] apply(double x, double y) {
        return new double[] {a * x + b * y + c, d * x + e * y + f};
    }
}
