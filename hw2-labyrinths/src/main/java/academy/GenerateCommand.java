package academy;

import academy.maze.Generator;
import academy.maze.display.MazePrinter;
import academy.maze.dto.Maze;
import academy.maze.factory.AlgorithmFactory;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "generate", description = "Generate a maze with specified algorithm and dimensions.")
public class GenerateCommand implements Callable<Integer> {

    private final MazePrinter mazePrinter = new MazePrinter();

    @Option(
            names = {"--algorithm", "-a"},
            required = true,
            description = "Generator algorithm: dfs, prim, kruskal")
    private String algorithm;

    @Option(
            names = {"--width", "-w"},
            required = true,
            description = "Maze width")
    private int width;

    @Option(
            names = {"--height", "-h"},
            required = true,
            description = "Maze height")
    private int height;

    @Option(
            names = {"--output", "-o"},
            description = "Output file path (optional)")
    private File output;

    @Option(
            names = {"--unicode", "-u"},
            description = "Use Unicode for display")
    private boolean useUnicode;

    @Override
    public Integer call() {
        try {
            if (output != null) {
                File parentDir = output.getParentFile();
                if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
                    throw new IOException("Could not create directory: " + parentDir.getPath());
                }
            }

            Generator generator = AlgorithmFactory.createGenerator(algorithm);
            Maze maze = generator.generate(width, height);

            if (output != null) {
                mazePrinter.saveMazeToFile(maze, output);
            } else {
                mazePrinter.printMazeToConsole(maze, useUnicode);
            }

            return 0;

        } catch (Exception e) {
            System.err.println("Error generating maze: " + e.getMessage());
            return 1;
        }
    }
}
