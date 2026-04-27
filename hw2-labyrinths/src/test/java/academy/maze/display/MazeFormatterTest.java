package academy.maze.display;

import static org.junit.jupiter.api.Assertions.*;

import academy.maze.dto.CellType;
import academy.maze.dto.Maze;
import academy.maze.dto.Path;
import academy.maze.dto.Point;
import org.junit.jupiter.api.Test;

class MazeFormatterTest {

    private final MazeFormatter formatter = new MazeFormatter();

    @Test
    void shouldFormatSimpleMazeWithoutUnicode() {
        CellType[][] cells = {
            {CellType.WALL, CellType.WALL, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.WALL},
            {CellType.WALL, CellType.WALL, CellType.WALL}
        };
        Maze maze = new Maze(cells);

        String result = formatter.formatBasicMaze(maze);

        assertNotNull(result);
        assertTrue(result.contains("#"));
        assertTrue(result.contains(" "));
    }

    @Test
    void shouldFormatMazeWithDifferentTerrain() {
        CellType[][] cells = {
            {CellType.WALL, CellType.WALL},
            {CellType.MUD, CellType.SWAMP}
        };
        Maze maze = new Maze(cells);

        String result = formatter.formatBasicMaze(maze);

        assertNotNull(result);
        assertTrue(result.contains(":"));
        assertTrue(result.contains("~"));
    }

    @Test
    void shouldFormatMazeWithPath() {
        CellType[][] cells = {
            {CellType.WALL, CellType.WALL, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.WALL},
            {CellType.WALL, CellType.WALL, CellType.WALL}
        };
        Maze maze = new Maze(cells);
        Path path = new Path(new Point[] {new Point(1, 1)});

        String result = formatter.formatMazeWithPathForConsole(maze, path);

        assertNotNull(result);
        assertTrue(result.contains("S"));
    }

    @Test
    void shouldFormatMazeWithStartAndEnd() {
        CellType[][] cells = {
            {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.PATH, CellType.WALL},
            {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL}
        };
        Maze maze = new Maze(cells);
        Point start = new Point(1, 1);
        Point end = new Point(2, 1);
        Path path = new Path(new Point[] {start, end});

        String result = formatter.formatMazeWithPathForConsole(maze, path);

        assertNotNull(result);
        assertTrue(result.contains("S"));
        assertTrue(result.contains("F"));
    }

    @Test
    void shouldFormatMazeWithPathPoints() {
        CellType[][] cells = {
            {CellType.WALL, CellType.WALL, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.WALL},
            {CellType.WALL, CellType.WALL, CellType.WALL}
        };
        Maze maze = new Maze(cells);
        Point[] points = {new Point(1, 1)};
        Path path = new Path(points);

        String result = formatter.formatMazeWithSolution(maze, path);

        assertNotNull(result);
        assertTrue(result.contains("O"));
        assertFalse(result.contains("S"));
    }

    @Test
    void shouldFormatMazeWithUnicode() {
        CellType[][] cells = {
            {CellType.WALL, CellType.PATH},
            {CellType.MUD, CellType.SWAMP}
        };
        Maze maze = new Maze(cells);

        String result = formatter.formatMazeWithUnicode(maze);

        assertNotNull(result);
        assertTrue(result.contains("██"));
        assertTrue(result.contains("░░"));
        assertTrue(result.contains("▓▓"));
        assertTrue(result.contains("  "));
    }

    @Test
    void shouldFormatMazeWithPathAndUnicode() {
        CellType[][] cells = {
            {CellType.WALL, CellType.WALL, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.WALL},
            {CellType.WALL, CellType.WALL, CellType.WALL}
        };
        Maze maze = new Maze(cells);
        Path path = new Path(new Point[] {new Point(1, 1)});

        String result = formatter.formatMazeWithPathAndUnicodeForConsole(maze, path);

        assertNotNull(result);
        assertTrue(result.contains("ST") || result.contains("FI"));
    }
}
