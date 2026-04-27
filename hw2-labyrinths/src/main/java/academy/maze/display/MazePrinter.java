package academy.maze.display;

import academy.maze.dto.Maze;
import academy.maze.dto.Path;
import java.io.*;

public class MazePrinter {
    private final MazeFormatter formatter;

    public MazePrinter(MazeFormatter formatter) {
        this.formatter = formatter;
    }

    public MazePrinter() {
        this(new MazeFormatter());
    }

    public void saveMazeToFile(Maze maze, File outputFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(outputFile)) {
            writer.print(formatter.formatBasicMaze(maze));
        }
    }

    public void saveSolutionToFile(Maze maze, Path path, File outputFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(outputFile)) {
            writer.print(formatter.formatMazeWithSolution(maze, path));
        }
    }

    public void printMazeToConsole(Maze maze, boolean useUnicode) {
        if (useUnicode) {
            System.out.print(formatter.formatMazeWithUnicode(maze));
        } else {
            System.out.print(formatter.formatBasicMaze(maze));
        }
    }

    public void printSolutionToConsole(Maze maze, Path path, boolean useUnicode) {
        if (useUnicode) {
            System.out.print(formatter.formatMazeWithPathAndUnicodeForConsole(maze, path));
        } else {
            System.out.print(formatter.formatMazeWithPathForConsole(maze, path));
        }
    }
}
