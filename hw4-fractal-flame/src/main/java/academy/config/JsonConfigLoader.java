package academy.config;

import academy.AppConfig;
import academy.affine.AffineTransform;
import academy.transformations.TransformationRegistry;
import academy.transformations.WeightedTransformation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class JsonConfigLoader {

    private JsonConfigLoader() {}

    public static AppConfig load(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(Path.of(path).toFile());

        AppConfig config = new AppConfig();

        config.width = root.get("size").get("width").asInt();
        config.height = root.get("size").get("height").asInt();
        config.iterationCount = root.get("iteration_count").asLong();
        config.threads = root.get("threads").asInt();
        config.outputPath = root.get("output_path").asText();

        JsonNode seedNode = root.get("seed");
        if (seedNode.isDouble()) {
            config.seed = Double.doubleToLongBits(seedNode.asDouble());
        } else {
            config.seed = seedNode.asLong();
        }

        config.symmetryLevel = getInt(root);
        config.gammaCorrection = getBoolean(root);
        config.gamma = getDouble(root, "gamma", 2.2);

        if (root.has("viewport")) {
            JsonNode viewport = root.get("viewport");
            config.viewportXmin = viewport.get("xmin").asDouble();
            config.viewportXmax = viewport.get("xmax").asDouble();
            config.viewportYmin = viewport.get("ymin").asDouble();
            config.viewportYmax = viewport.get("ymax").asDouble();
        } else {
            config.viewportXmin = -2.0;
            config.viewportXmax = 2.0;
            config.viewportYmin = -2.0;
            config.viewportYmax = 2.0;
        }

        List<WeightedTransformation> functions = new ArrayList<>();
        for (JsonNode funcNode : root.get("functions")) {
            String name = funcNode.get("name").asText();
            double weight = funcNode.get("weight").asDouble();

            double r = getDouble(funcNode, "r", 128) / 255.0;
            double g = getDouble(funcNode, "g", 128) / 255.0;
            double b = getDouble(funcNode, "b", 128) / 255.0;

            functions.add(new WeightedTransformation(TransformationRegistry.byName(name), weight, r, g, b));
        }
        config.transformations = functions;

        List<AffineTransform> affines = new ArrayList<>();
        JsonNode affineNode = root.get("affine_params");

        if (affineNode.isArray()) {
            for (JsonNode affNode : affineNode) {
                affines.add(parseAffine(affNode));
            }
        } else {
            affines.add(parseAffine(affineNode));
        }
        config.affineTransforms = affines;
        return config;
    }

    private static AffineTransform parseAffine(JsonNode node) {
        return new AffineTransform(
                node.get("a").asDouble(),
                node.get("b").asDouble(),
                node.get("c").asDouble(),
                node.get("d").asDouble(),
                node.get("e").asDouble(),
                node.get("f").asDouble());
    }

    private static int getInt(JsonNode node) {
        JsonNode fieldNode = node.get("symmetry_level");
        return fieldNode != null ? fieldNode.asInt() : 1;
    }

    private static double getDouble(JsonNode node, String field, double defaultValue) {
        JsonNode fieldNode = node.get(field);
        return fieldNode != null ? fieldNode.asDouble() : defaultValue;
    }

    private static boolean getBoolean(JsonNode node) {
        JsonNode fieldNode = node.get("gamma_correction");
        return fieldNode == null || fieldNode.asBoolean();
    }
}
