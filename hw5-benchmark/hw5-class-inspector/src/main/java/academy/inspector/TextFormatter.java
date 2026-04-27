package academy.inspector;

import academy.hierarchy.HierarchyBuilder;
import academy.reflection.ReflectionUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TextFormatter {

    public static String inspect(Class<?> clazz) {
        StringBuilder sb = new StringBuilder();

        sb.append("Class: ").append(clazz.getName()).append('\n');

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            sb.append("Superclass: ").append(superclass.getSimpleName()).append('\n');
        }

        sb.append("Interfaces:\n");
        for (Class<?> i : clazz.getInterfaces()) {
            sb.append("  - ").append(i.getSimpleName()).append('\n');
        }

        sb.append("Fields:\n");
        for (Field f : clazz.getDeclaredFields()) {
            sb.append("  - ")
                    .append(ReflectionUtils.accessModifier(f.getModifiers()))
                    .append(' ')
                    .append(f.getName())
                    .append(" (")
                    .append(f.getType().getSimpleName())
                    .append(")\n");
        }

        sb.append("Methods:\n");
        for (Method m : clazz.getDeclaredMethods()) {
            sb.append("  - ")
                    .append(ReflectionUtils.accessModifier(m.getModifiers()))
                    .append(' ')
                    .append(m.getName())
                    .append('(')
                    .append(ReflectionUtils.joinParamTypes(m.getParameters()))
                    .append(") : ")
                    .append(m.getReturnType().getSimpleName())
                    .append('\n');
        }

        sb.append("Annotations:\n");
        for (Annotation a : clazz.getAnnotations()) {
            sb.append("  - @").append(a.annotationType().getSimpleName()).append('\n');
        }

        sb.append("Hierarchy:\n");
        sb.append(HierarchyBuilder.buildTextHierarchy(clazz));

        return sb.toString();
    }
}
