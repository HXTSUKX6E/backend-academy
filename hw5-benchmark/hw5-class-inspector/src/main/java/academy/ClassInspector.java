package academy;

import academy.inspector.JsonFormatter;
import academy.inspector.TextFormatter;

public class ClassInspector {

    public static String inspect(Class<?> clazz, String format) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz must not be null");
        }
        if (format == null) {
            throw new IllegalArgumentException("format must not be null");
        }

        String fmt = format.toUpperCase();
        return switch (fmt) {
            case "TEXT" -> TextFormatter.inspect(clazz);
            case "JSON" -> JsonFormatter.inspect(clazz);
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        };
    }
}
