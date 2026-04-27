package academy.core;

import java.util.Arrays;

public class Histogram {

    public final int width;
    public final int height;

    private final float[] data;

    public Histogram(int width, int height) {
        this.width = width;
        this.height = height;
        this.data = new float[width * height * 4];
    }

    public void hit(int x, int y, double r, double g, double b) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;

        int index = (y * width + x) * 4;
        data[index] += r;
        data[index + 1] += g;
        data[index + 2] += b;
        data[index + 3] += 1.0f;
    }

    public void addFrom(Histogram other) {
        if (other.width != this.width || other.height != this.height) {
            throw new IllegalArgumentException("Histogram sizes do not match");
        }
        for (int i = 0; i < data.length; i++) {
            this.data[i] += other.data[i];
        }
    }

    public double maxDensity() {
        double max = 0.0;
        for (int i = 3; i < data.length; i += 4) {
            float density = data[i];
            if (density > max) {
                max = density;
            }
        }
        return max;
    }

    public float[] get(int x, int y) {
        int index = (y * width + x) * 4;
        return new float[] {data[index], data[index + 1], data[index + 2], data[index + 3]};
    }

    public void clear() {
        Arrays.fill(data, 0.0f);
    }
}
