package academy.transformations;

import academy.transformations.impl.*;
import java.util.HashMap;
import java.util.Map;

public final class TransformationRegistry {

    private static final Map<String, Transformation> MAP = new HashMap<>();

    static {
        register(new LinearTransformation());
        register(new SinusoidalTransformation());
        register(new SphericalTransformation());
        register(new SwirlTransformation());
        register(new HorseshoeTransformation());
        register(new CurlTransformation());
        register(new PolarTransformation());
        register(new DiscTransformation());
        register(new SpiralTransformation());
    }

    private static void register(Transformation t) {
        MAP.put(t.name(), t);
    }

    public static Transformation byName(String name) {
        Transformation t = MAP.get(name.toLowerCase());
        if (t == null) {
            throw new IllegalArgumentException("Unknown transformation: " + name);
        }
        return t;
    }

    private TransformationRegistry() {}
}
