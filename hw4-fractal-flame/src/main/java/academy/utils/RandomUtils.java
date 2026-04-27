package academy.utils;

import academy.transformations.WeightedTransformation;
import java.util.List;
import java.util.Random;

public final class RandomUtils {

    private RandomUtils() {}

    public static WeightedTransformation weighted(List<WeightedTransformation> list, Random rnd) {
        double sum = 0.0;
        for (WeightedTransformation wt : list) {
            sum += wt.weight();
        }

        double r = rnd.nextDouble() * sum;
        double acc = 0.0;

        for (WeightedTransformation wt : list) {
            acc += wt.weight();
            if (r <= acc) {
                return wt;
            }
        }
        return list.getLast();
    }

    public static WeightedTransformationWithIndex weightedWithIndex(List<WeightedTransformation> list, Random rnd) {
        double sum = 0.0;
        for (WeightedTransformation wt : list) {
            sum += wt.weight();
        }

        double r = rnd.nextDouble() * sum;
        double acc = 0.0;

        for (int i = 0; i < list.size(); i++) {
            WeightedTransformation wt = list.get(i);
            acc += wt.weight();
            if (r <= acc) {
                return new WeightedTransformationWithIndex(wt, i);
            }
        }
        WeightedTransformation last = list.getLast();
        return new WeightedTransformationWithIndex(last, list.size() - 1);
    }

    public record WeightedTransformationWithIndex(WeightedTransformation transformation, int index) {}
}
