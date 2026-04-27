package academy;

import academy.config.CliArgsParser;
import academy.config.DefaultsConfig;
import academy.config.JsonConfigLoader;
import academy.service.GenerationService;
import academy.validation.ConfigValidator;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Application {

    private static final Logger logger = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        try {
            logger.info("Starting Fractal Flame Generator");
            args = normalizeArgs(args);
            logger.debug("Normalized args: {}", (Object) args);

            AppConfig config = DefaultsConfig.create();

            for (int i = 0; i < args.length - 1; i++) {
                if (args[i].equals("--config")) {
                    String configPath = args[i + 1];
                    logger.info("Loading configuration from: {}", configPath);
                    try {
                        config = JsonConfigLoader.load(configPath);
                        logger.info("Configuration loaded successfully");
                    } catch (IOException e) {
                        logger.error("Failed to load configuration from {}: {}", configPath, e.getMessage());
                        System.exit(1);
                    }
                    break;
                }
            }

            CliArgsParser.parse(config, args);
            logger.info("CLI arguments parsed");

            ConfigValidator.validate(config);
            logger.info("Configuration validated successfully");

            logger.info("Starting generation service");
            new GenerationService().generate(config);

            logger.info("Fractal generation completed successfully");

        } catch (Exception e) {
            logger.error("Application failed: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    static String[] normalizeArgs(String[] args) {
        if (args.length == 1 && args[0].contains(" ")) {
            return args[0].trim().split("\\s+");
        }
        return args;
    }
}
