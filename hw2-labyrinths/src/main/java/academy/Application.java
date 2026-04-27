package academy;

import academy.maze.display.MazePrinter;
import academy.maze.ui.InteractiveMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "maze-app",
        description = "Maze generator and solver CLI application.",
        mixinStandardHelpOptions = true,
        subcommands = {GenerateCommand.class, SolveCommand.class})
public class Application implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    private final MazePrinter mazePrinter = new MazePrinter();

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        InteractiveMode interactiveMode = new InteractiveMode(mazePrinter);
        interactiveMode.start();
    }
}
