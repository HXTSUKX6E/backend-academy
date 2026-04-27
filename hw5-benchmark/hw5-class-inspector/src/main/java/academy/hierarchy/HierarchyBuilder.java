package academy.hierarchy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HierarchyBuilder {

    public static String buildTextHierarchy(Class<?> clazz) {
        return TreePrinter.printTextHierarchy(clazz);
    }

    public static Map<String, Object> buildJsonHierarchy(Class<?> clazz) {
        List<Class<?>> chain = getSuperclassChainTopDown(clazz);
        chain = dropObjectRootIfPresent(chain);

        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> curMap = root;

        for (int i = 0; i < chain.size(); i++) {
            Class<?> node = chain.get(i);
            Map<String, Object> next = new LinkedHashMap<>();
            curMap.put(node.getSimpleName(), next);
            curMap = next;
        }

        if (clazz.isSealed()) {
            for (Class<?> child : clazz.getPermittedSubclasses()) {
                curMap.put(child.getSimpleName(), buildSealedSubtreeJson(child));
            }
        }

        return root;
    }

    static List<Class<?>> dropObjectRootIfPresent(List<Class<?>> chain) {
        if (chain.size() >= 2 && chain.getFirst() == Object.class) {
            return chain.subList(1, chain.size());
        }
        return chain;
    }

    public static List<Class<?>> getSuperclassChainTopDown(Class<?> clazz) {
        List<Class<?>> chain = new ArrayList<>();
        Class<?> cur = clazz;
        while (cur != null) {
            chain.add(cur);
            cur = cur.getSuperclass();
        }
        List<Class<?>> res = new ArrayList<>();
        for (int i = chain.size() - 1; i >= 0; i--) {
            res.add(chain.get(i));
        }
        return res;
    }

    private static Map<String, Object> buildSealedSubtreeJson(Class<?> clazz) {
        Map<String, Object> node = new LinkedHashMap<>();
        if (!clazz.isSealed()) {
            return node; // leaf {}
        }
        Class<?>[] permitted = clazz.getPermittedSubclasses();
        if (permitted == null) {
            return node;
        }
        for (Class<?> child : permitted) {
            node.put(child.getSimpleName(), buildSealedSubtreeJson(child));
        }
        return node;
    }
}
