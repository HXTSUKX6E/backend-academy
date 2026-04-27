package academy.inspector;

import academy.hierarchy.HierarchyBuilder;
import academy.reflection.ReflectionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonFormatter {

    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static String inspect(Class<?> clazz) {
        Map<String, Object> root = buildJsonStructure(clazz);

        try {
            return MAPPER.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }
    }

    private static Map<String, Object> buildJsonStructure(Class<?> clazz) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("class", clazz.getName());

        Class<?> superclass = clazz.getSuperclass();
        root.put("superclass", superclass == null ? null : superclass.getSimpleName());

        List<String> interfaces = new ArrayList<>();
        for (Class<?> i : clazz.getInterfaces()) {
            interfaces.add(i.getSimpleName());
        }
        root.put("interfaces", interfaces);

        List<Map<String, Object>> fields = new ArrayList<>();
        for (Field f : clazz.getDeclaredFields()) {
            Map<String, Object> fi = new LinkedHashMap<>();
            fi.put("access", ReflectionUtils.accessModifier(f.getModifiers()));
            fi.put("name", f.getName());
            fi.put("type", f.getType().getSimpleName());
            fields.add(fi);
        }
        root.put("fields", fields);

        List<Map<String, Object>> methods = new ArrayList<>();
        for (Method m : clazz.getDeclaredMethods()) {
            Map<String, Object> mi = new LinkedHashMap<>();
            mi.put("access", ReflectionUtils.accessModifier(m.getModifiers()));
            mi.put("name", m.getName());

            List<String> params = new ArrayList<>();
            for (java.lang.reflect.Parameter p : m.getParameters()) {
                params.add(p.getType().getSimpleName());
            }
            mi.put("params", params);
            mi.put("returnType", m.getReturnType().getSimpleName());
            methods.add(mi);
        }
        root.put("methods", methods);

        List<String> annotations = new ArrayList<>();
        for (Annotation a : clazz.getAnnotations()) {
            annotations.add(a.annotationType().getSimpleName());
        }
        root.put("annotations", annotations);

        Map<String, Object> hierarchy = HierarchyBuilder.buildJsonHierarchy(clazz);
        root.put("hierarchy", hierarchy);

        return root;
    }
}
