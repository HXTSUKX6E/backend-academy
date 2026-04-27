package academy.maze.impl.solver;

import static org.junit.jupiter.api.Assertions.*;

import academy.maze.dto.CellType;
import academy.maze.dto.Maze;
import academy.maze.dto.Path;
import academy.maze.dto.Point;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AStarSolverTest {

    private AStarSolver solver;

    @BeforeEach
    void setUp() {
        solver = new AStarSolver();
    }

    @Test
    void shouldFindPathInSimpleMaze() {
        CellType[][] cells = {
            {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.PATH, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.PATH, CellType.WALL},
            {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL}
        };
        Maze maze = new Maze(cells);
        Point start = new Point(1, 1);
        Point end = new Point(2, 2);
        Path path = solver.solve(maze, start, end);
        assertNotNull(path, "Путь null");
        assertTrue(path.points().length > 0, "Путь пустой");
        assertEquals(start, path.points()[0], "Начало не совпадает");
        assertEquals(end, path.points()[path.points().length - 1], "Конец не совпадает");
    }

    @Test
    void shouldFindShortestPath() {
        CellType[][] cells = {
            {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.PATH, CellType.PATH, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.WALL, CellType.PATH, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.PATH, CellType.PATH, CellType.WALL},
            {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL}
        };
        Maze maze = new Maze(cells);
        Point start = new Point(1, 1);
        Point end = new Point(3, 3);
        Path path = solver.solve(maze, start, end);
        assertNotNull(path, "Путь null");
        assertEquals(5, path.points().length, "Длина пути неверна");
    }

    @Test
    void shouldConsiderTerrainCost() {
        CellType[][] cells = {
            {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.MUD, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.SWAMP, CellType.WALL},
            {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL}
        };
        Maze maze = new Maze(cells);
        Point start = new Point(1, 1);
        Point end = new Point(2, 2);
        Path path = solver.solve(maze, start, end);
        assertNotNull(path, "Путь null");
        assertTrue(path.points().length >= 3, "Путь слишком короткий");
    }

    @Test
    void shouldReturnEmptyPathWhenNoSolution() {
        CellType[][] cells = {
            {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.WALL, CellType.WALL},
            {CellType.WALL, CellType.WALL, CellType.PATH, CellType.WALL},
            {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL}
        };
        Maze maze = new Maze(cells);
        Point start = new Point(1, 1);
        Point end = new Point(2, 2);
        Path path = solver.solve(maze, start, end);
        assertNotNull(path, "Путь null");
        assertEquals(0, path.points().length, "Путь не пустой");
    }

    @Test
    void shouldHandleStartEqualsEnd() {
        CellType[][] cells = {
            {CellType.WALL, CellType.WALL, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.WALL},
            {CellType.WALL, CellType.WALL, CellType.WALL}
        };
        Maze maze = new Maze(cells);
        Point point = new Point(1, 1);
        Path path = solver.solve(maze, point, point);
        assertNotNull(path, "Путь null");
        assertEquals(1, path.points().length, "Длина пути неверна");
        assertEquals(point, path.points()[0], "Точка не совпадает");
    }

    @Test
    void shouldFindPathAroundWalls() {
        Maze maze = getMaze();
        Point start = new Point(1, 1);
        Point end = new Point(3, 1);
        Path path = solver.solve(maze, start, end);
        assertNotNull(path, "Путь null");
        assertTrue(path.points().length > 2, "Путь слишком короткий");
        assertEquals(start, path.points()[0], "Начало не совпадает");
        assertEquals(end, path.points()[path.points().length - 1], "Конец не совпадает");
    }

    private static @NotNull Maze getMaze() {
        CellType[][] cells = {
            {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.WALL, CellType.PATH, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.WALL, CellType.PATH, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.PATH, CellType.PATH, CellType.WALL},
            {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL}
        };
        return new Maze(cells);
    }

    @Test
    void shouldPreferCheaperTerrain() {
        CellType[][] cells = {
            {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.SWAMP, CellType.SWAMP, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.PATH, CellType.PATH, CellType.WALL},
            {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL}
        };
        Maze maze = new Maze(cells);
        Point start = new Point(1, 1);
        Point end = new Point(3, 2);
        Path path = solver.solve(maze, start, end);
        assertNotNull(path, "Путь null");
        boolean usesBottomPath = false;
        for (Point point : path.points()) {
            if (point.y() == 2 && point.x() >= 1 && point.x() <= 3) {
                usesBottomPath = true;
                break;
            }
        }
        assertTrue(usesBottomPath, "Не использован более дешевый путь через низ");
        boolean usesExpensivePath = false;
        for (Point point : path.points()) {
            if (point.y() == 1 && (point.x() == 2 || point.x() == 3)) {
                usesExpensivePath = true;
                break;
            }
        }
        assertFalse(usesExpensivePath, "Использован дорогой путь через SWAMP");
    }

    @Test
    void shouldHandleLargeMaze() {
        int size = 10;
        CellType[][] cells = new CellType[size][size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                cells[y][x] = (x == 0 || x == size - 1 || y == 0 || y == size - 1) ? CellType.WALL : CellType.PATH;
            }
        }
        Maze maze = new Maze(cells);
        Point start = new Point(1, 1);
        Point end = new Point(size - 2, size - 2);
        Path path = solver.solve(maze, start, end);
        assertNotNull(path, "Путь null");
        assertTrue(path.points().length > 0, "Путь пустой");
        assertEquals(start, path.points()[0], "Начало не совпадает");
        assertEquals(end, path.points()[path.points().length - 1], "Конец не совпадает");
    }
}
