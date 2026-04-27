package academy.hierarchy;

import java.util.List;

public class TreePrinter {

    public static String printTextHierarchy(Class<?> clazz) {
        StringBuilder sb = new StringBuilder();
        List<Class<?>> chain = HierarchyBuilder.getSuperclassChainTopDown(clazz);
        chain = HierarchyBuilder.dropObjectRootIfPresent(chain);

        sb.append(chain.getFirst().getSimpleName()).append('\n');

        for (int i = 1; i < chain.size(); i++) {
            sb.append("  ".repeat(i))
                    .append("└── ")
                    .append(chain.get(i).getSimpleName())
                    .append('\n');
        }

        String indentForChildren = "  ".repeat(chain.size());
        appendSealedSubclassesTree(sb, clazz, indentForChildren);

        return sb.toString();
    }

    private static void appendSealedSubclassesTree(StringBuilder sb, Class<?> clazz, String indent) {
        if (!clazz.isSealed()) {
            return;
        }
        Class<?>[] permitted = clazz.getPermittedSubclasses();
        if (permitted == null) {
            return;
        }

        for (Class<?> child : permitted) {
            sb.append(indent).append("└── ").append(child.getSimpleName()).append('\n');
            appendSealedSubclassesTree(sb, child, indent + "  ");
        }
    }
}
