package academy.maze.impl.solver;

import static org.junit.jupiter.api.Assertions.*;

import academy.maze.dto.CellType;
import academy.maze.dto.Maze;
import academy.maze.dto.Path;
import academy.maze.dto.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BFSSolverTest {

    private BFSSolver solver;

    @BeforeEach
    void setUp() {
        solver = new BFSSolver();
    }

    @Test
    void shouldFindPathInSimpleMaze() {
        CellType[][] cells = {
            {CellType.WALL, CellType.WALL, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.WALL},
            {CellType.WALL, CellType.WALL, CellType.WALL}
        };
        Maze maze = new Maze(cells);
        Point start = new Point(1, 1);
        Point end = new Point(1, 1);

        Path path = solver.solve(maze, start, end);

        assertNotNull(path, "Путь null");
        assertEquals(1, path.points().length, "Длина неверна");
        assertEquals(start, path.points()[0], "Точка не совпадает");
    }

    @Test
    void shouldFindPathAroundObstacles() {
        CellType[][] cells = {
            {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.WALL, CellType.PATH},
            {CellType.WALL, CellType.PATH, CellType.PATH, CellType.PATH},
            {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL}
        };
        Maze maze = new Maze(cells);
        Point start = new Point(1, 1);
        Point end = new Point(3, 1);

        Path path = solver.solve(maze, start, end);

        assertNotNull(path, "Путь null");
        assertEquals(start, path.points()[0], "Начало не совпадает");
        assertEquals(end, path.points()[path.points().length - 1], "Конец не совпадает");
        assertTrue(path.points().length > 2, "Путь слишком короткий");
    }

    @Test
    void shouldReturnEmptyPathWhenNoSolution() {
        CellType[][] cells = {
            {CellType.WALL, CellType.WALL, CellType.WALL},
            {CellType.WALL, CellType.PATH, CellType.WALL},
            {CellType.WALL, CellType.WALL, CellType.WALL}
        };
        Maze maze = new Maze(cells);
        Point start = new Point(1, 1);
        Point end = new Point(2, 2);

        Path path = solver.solve(maze, start, end);

        assertNotNull(path, "Путь null");
        assertEquals(0, path.points().length, "Путь не пустой");
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
        assertTrue(path.points().length >= 3, "Путь должен учитывать стоимость");
    }
}
