package academy.reflection;

import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

public class ReflectionUtils {

    public static String accessModifier(int mods) {
        if (Modifier.isPublic(mods)) {
            return "public";
        }
        if (Modifier.isProtected(mods)) {
            return "protected";
        }
        if (Modifier.isPrivate(mods)) {
            return "private";
        }
        return "package-private";
    }

    public static String joinParamTypes(Parameter[] params) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(params[i].getType().getSimpleName());
        }
        return sb.toString();
    }
}
