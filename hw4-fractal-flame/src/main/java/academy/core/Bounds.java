package academy.core;

public class Bounds {

    private double minX = Double.MAX_VALUE;
    private double maxX = -Double.MAX_VALUE;
    private double minY = Double.MAX_VALUE;
    private double maxY = -Double.MAX_VALUE;

    public void update(double x, double y) {
        minX = Math.min(minX, x);
        maxX = Math.max(maxX, x);
        minY = Math.min(minY, y);
        maxY = Math.max(maxY, y);
    }

    public void expand(double factor) {
        double width = maxX - minX;
        double height = maxY - minY;

        double expandX = width * factor / 2.0;
        double expandY = height * factor / 2.0;

        minX -= expandX;
        maxX += expandX;
        minY -= expandY;
        maxY += expandY;
    }

    public void reset(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public double minX() {
        return minX;
    }

    public double maxX() {
        return maxX;
    }

    public double minY() {
        return minY;
    }

    public double maxY() {
        return maxY;
    }
}
