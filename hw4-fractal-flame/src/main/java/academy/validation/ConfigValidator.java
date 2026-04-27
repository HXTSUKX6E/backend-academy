package academy.validation;

import academy.AppConfig;
import academy.exceptions.ValidationException;

public final class ConfigValidator {

    private ConfigValidator() {}

    public static void validate(AppConfig c) {
        if (c.width <= 0 || c.height <= 0) throw new ValidationException("Invalid image size");

        if (c.iterationCount <= 0) throw new ValidationException("Iteration count must be > 0");

        if (c.threads <= 0) throw new ValidationException("Threads must be >= 1");

        if (c.affineTransforms.isEmpty()) throw new ValidationException("No affine transforms");

        if (c.transformations.isEmpty()) throw new ValidationException("No functions");
    }
}
