package academy.maze.display;

import static org.junit.jupiter.api.Assertions.*;

import academy.maze.dto.CellType;
import academy.maze.dto.Maze;
import academy.maze.dto.Path;
import academy.maze.dto.Point;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

class MazePrinterTest {

    @Test
    void saveMazeToFile_ValidMaze_FileCreated() throws IOException {
        MazePrinter printer = new MazePrinter();
        CellType[][] cells = {
            {CellType.PATH, CellType.PATH},
            {CellType.PATH, CellType.PATH}
        };
        Maze maze = new Maze(cells);

        File outputFile = File.createTempFile("test_maze", ".txt");
        outputFile.deleteOnExit();

        printer.saveMazeToFile(maze, outputFile);

        assertTrue(outputFile.exists());
        String content = Files.readString(outputFile.toPath());
        assertFalse(content.isEmpty());
    }

    @Test
    void saveSolutionToFile_ValidSolution_FileCreated() throws IOException {
        MazePrinter printer = new MazePrinter();
        CellType[][] cells = {
            {CellType.PATH, CellType.PATH},
            {CellType.PATH, CellType.PATH}
        };
        Maze maze = new Maze(cells);
        Path path = new Path(new Point[] {new Point(0, 0), new Point(1, 1)});

        File outputFile = File.createTempFile("test_solution", ".txt");
        outputFile.deleteOnExit();

        printer.saveSolutionToFile(maze, path, outputFile);

        assertTrue(outputFile.exists());
        String content = Files.readString(outputFile.toPath());
        assertFalse(content.isEmpty());
    }

    @Test
    void shouldUseDefaultConstructor() {
        MazePrinter printer = new MazePrinter();

        assertNotNull(printer, "Принтер не создан");
    }

    @Test
    void shouldPrintMazeToConsole() {
        MazeFormatter formatter = new MazeFormatter();
        MazePrinter printer = new MazePrinter(formatter);

        CellType[][] cells = {
            {CellType.WALL, CellType.PATH},
            {CellType.MUD, CellType.SWAMP}
        };
        Maze maze = new Maze(cells);

        assertDoesNotThrow(() -> printer.printMazeToConsole(maze, false), "Ошибка при выводе в консоль");
    }

    @Test
    void shouldPrintSolutionToConsole() {
        MazeFormatter formatter = new MazeFormatter();
        MazePrinter printer = new MazePrinter(formatter);

        CellType[][] cells = {
            {CellType.WALL, CellType.WALL, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.WALL},
            {CellType.WALL, CellType.WALL, CellType.WALL}
        };
        Maze maze = new Maze(cells);
        Path path = new Path(new Point[] {new Point(1, 1)});

        assertDoesNotThrow(
                () -> printer.printSolutionToConsole(maze, path, false), "Ошибка при выводе решения в консоль");
    }

    @Test
    void shouldHandleUnicodeInConsole() {
        MazeFormatter formatter = new MazeFormatter();
        MazePrinter printer = new MazePrinter(formatter);

        CellType[][] cells = {
            {CellType.WALL, CellType.PATH},
            {CellType.MUD, CellType.SWAMP}
        };
        Maze maze = new Maze(cells);

        assertDoesNotThrow(() -> printer.printMazeToConsole(maze, true), "Ошибка при unicode выводе");
    }
}
