package academy.transformations;

public interface Transformation {

    String name();

    double[] apply(double x, double y);
}
