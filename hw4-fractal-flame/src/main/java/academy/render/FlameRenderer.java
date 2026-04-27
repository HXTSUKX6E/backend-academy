package academy.render;

import academy.AppConfig;
import academy.core.Histogram;
import java.awt.image.BufferedImage;

public final class FlameRenderer {

    public BufferedImage render(AppConfig c, Histogram h) {
        int width = h.width;
        int height = h.height;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        double maxDensity = h.maxDensity();
        System.out.printf("RENDER: width=%d, height=%d, maxDensity=%.2f%n", width, height, maxDensity);

        if (maxDensity <= 0.0) {
            System.out.println("ERROR: maxDensity = 0");
            return image;
        }

        double rootPower = 0.3;
        double brightness = c.brightness > 0 ? c.brightness : 3.0;
        double minVisibleDensity = 1.0;

        int nonZeroPixels = 0;
        double totalDensity = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (h.get(x, y)[3] > 0) {
                    nonZeroPixels++;
                    totalDensity += h.get(x, y)[3];
                }
            }
        }
        System.out.printf(
                "Non-zero pixels: %d/%d (%.1f%%), avg density: %.2f%n",
                nonZeroPixels, width * height, nonZeroPixels * 100.0 / (width * height), totalDensity / nonZeroPixels);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float[] cell = h.get(x, y);
                double density = cell[3];

                if (density <= 0.0) {
                    image.setRGB(x, y, 0x000000);
                    continue;
                }

                double normalized;
                if (maxDensity > 1.0) {
                    normalized = Math.pow(density / maxDensity, rootPower);
                } else {
                    normalized = density;
                }

                if (density >= minVisibleDensity) {
                    double minIntensity = 0.1;
                    normalized = Math.max(normalized, minIntensity);
                }

                normalized *= brightness;
                normalized = Math.min(1.0, Math.max(0.0, normalized));

                if (x % 100 == 0 && y % 100 == 0 && density > 0) {
                    System.out.printf("  Pixel[%d,%d]: density=%.0f, normalized=%.3f%n", x, y, density, normalized);
                }

                double r = cell[0] / Math.max(density, 0.001);
                double g = cell[1] / Math.max(density, 0.001);
                double b = cell[2] / Math.max(density, 0.001);

                r = Math.min(1.0, Math.pow(r, 0.8));
                g = Math.min(1.0, Math.pow(g, 0.8));
                b = Math.min(1.0, Math.pow(b, 0.8));

                r *= normalized;
                g *= normalized;
                b *= normalized;

                if (c.gammaCorrection && c.gamma > 0) {
                    double gammaInv = 1.0 / c.gamma;
                    r = Math.pow(r, gammaInv);
                    g = Math.pow(g, gammaInv);
                    b = Math.pow(b, gammaInv);
                }

                int ir = clamp255(r * 255.0);
                int ig = clamp255(g * 255.0);
                int ib = clamp255(b * 255.0);

                image.setRGB(x, y, (ir << 16) | (ig << 8) | ib);
            }
        }

        System.out.println("Rendering complete");
        return image;
    }

    private static int clamp255(double v) {
        if (v <= 0.0) return 0;
        if (v >= 255.0) return 255;
        return (int) v;
    }
}
