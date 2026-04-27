package academy.utils;

import academy.transformations.Transformation;
import academy.transformations.TransformationRegistry;
import academy.transformations.WeightedTransformation;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class FunctionParser {

    private FunctionParser() {}

    public static List<WeightedTransformation> parse(String value, long seed) {
        Random rnd = new Random(seed);
        List<WeightedTransformation> list = new ArrayList<>();

        String[] parts = value.split(",");

        for (String part : parts) {
            String[] kv = part.split(":");

            if (kv.length != 2 && kv.length != 5) {
                throw new IllegalArgumentException(
                        "Invalid function format: " + part + ". Expected: name:weight OR name:weight:r:g:b");
            }

            String name = kv[0].trim();
            double weight = Double.parseDouble(kv[1].trim());

            double r, g, b;

            if (kv.length == 5) {
                r = Integer.parseInt(kv[2]) / 255.0;
                g = Integer.parseInt(kv[3]) / 255.0;
                b = Integer.parseInt(kv[4]) / 255.0;
            } else {
                r = rnd.nextDouble();
                g = rnd.nextDouble();
                b = rnd.nextDouble();
            }

            Transformation transformation = TransformationRegistry.byName(name);

            list.add(new WeightedTransformation(transformation, weight, r, g, b));
        }

        return list;
    }
}
