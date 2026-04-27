package academy.utils;

import academy.affine.AffineTransform;
import java.util.ArrayList;
import java.util.List;

public final class AffineParser {

    private AffineParser() {}

    public static List<AffineTransform> parse(String value) {

        if (value == null) {
            throw new IllegalArgumentException("Affine params string is null");
        }

        List<AffineTransform> result = new ArrayList<>();

        String[] affineParts = value.split(";");

        for (String part : affineParts) {

            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            String[] v = trimmed.split(",");

            if (v.length != 6) {
                throw new IllegalArgumentException("Invalid affine params: " + trimmed + ". Expected: a,b,c,d,e,f");
            }

            result.add(new AffineTransform(
                    Double.parseDouble(v[0]),
                    Double.parseDouble(v[1]),
                    Double.parseDouble(v[2]),
                    Double.parseDouble(v[3]),
                    Double.parseDouble(v[4]),
                    Double.parseDouble(v[5])));
        }

        return result;
    }
}
