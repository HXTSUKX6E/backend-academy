package academy;

import academy.maze.Solver;
import academy.maze.display.MazePrinter;
import academy.maze.dto.Maze;
import academy.maze.dto.Path;
import academy.maze.dto.Point;
import academy.maze.factory.AlgorithmFactory;
import academy.maze.utils.MazeUtils;
import academy.maze.utils.PointUtils;
import java.io.File;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "solve", description = "Solve a maze with specified algorithm and points.")
public class SolveCommand implements Callable<Integer> {

    private final MazePrinter mazePrinter = new MazePrinter();

    @Option(
            names = {"--algorithm", "-a"},
            required = true,
            description = "Solver algorithm: astar, dijkstra, bfs")
    private String algorithm;

    @Option(
            names = {"--file", "-f"},
            required = true,
            description = "Maze file path")
    private File mazeFile;

    @Option(
            names = {"--start", "-s"},
            required = true,
            description = "Start point: x,y")
    private String start;

    @Option(
            names = {"--end", "-e"},
            required = true,
            description = "End point: x,y")
    private String end;

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
            Point startPoint = PointUtils.parsePoint(start);
            Point endPoint = PointUtils.parsePoint(end);
            Maze maze = MazeUtils.loadMazeFromFile(mazeFile);

            MazeUtils.validatePoint(maze, startPoint, "Start");
            MazeUtils.validatePoint(maze, endPoint, "End");

            Solver solver = AlgorithmFactory.createSolver(algorithm);
            Path path = solver.solve(maze, startPoint, endPoint);

            if (output != null) {
                mazePrinter.saveSolutionToFile(maze, path, output);
            } else {
                mazePrinter.printSolutionToConsole(maze, path, useUnicode);
            }
            return 0;

        } catch (Exception e) {
            if (e.getMessage().contains("Point must be in format")) {
                System.err.println("Invalid point format: " + start + ", expected format: x,y");
            } else {
                System.err.println("Error solving maze: " + e.getMessage());
            }
            return 1;
        }
    }
}
