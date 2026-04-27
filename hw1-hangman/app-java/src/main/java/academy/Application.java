package academy;

import academy.hangman.game.HangmanGame;
import academy.hangman.game.NonInteractiveGame;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import static java.util.Objects.nonNull;

@Command(name = "Application Example", version = "Example 1.0", mixinStandardHelpOptions = true)
public class Application implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private static final ObjectReader YAML_READER =
        new ObjectMapper(new YAMLFactory()).findAndRegisterModules().reader();
    private static final Predicate<String[]> IS_TESTING_MODE = words -> nonNull(words) && words.length == 2;

    @Option(
        names = {"-s", "--font-size"},
        description = "Font size")
    int fontSize;

    @Parameters(
        paramLabel = "<word>",
        description = "Words pair for testing mode")
    private String[] words;

    @Option(
        names = {"-c", "--config"},
        description = "Path to YAML config file")
    private File configPath;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        AppConfig config = loadConfig();
        LOGGER.atInfo().addKeyValue("config", config).log("Config content");

        if (IS_TESTING_MODE.test(config.words())) {
            LOGGER.atInfo().log("Non-interactive testing mode enabled");
            try {
                String secretWord = config.words()[0];
                String userInput = config.words()[1];
                NonInteractiveGame game = new NonInteractiveGame(secretWord, userInput);
                game.startGame();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else {
            LOGGER.atInfo().log("Interactive mode enabled");
            HangmanGame game = new HangmanGame();
            game.startGame();
        }
    }

    private AppConfig loadConfig() {
        // fill with cli options
        if (configPath == null) return new AppConfig(fontSize, words);

        // use config file if provided
        try {
            return YAML_READER.readValue(configPath, AppConfig.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
