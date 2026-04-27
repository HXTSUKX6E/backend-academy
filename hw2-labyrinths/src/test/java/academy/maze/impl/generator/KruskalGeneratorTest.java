package academy.maze.impl.generator;

import static org.junit.jupiter.api.Assertions.*;

import academy.maze.dto.CellType;
import academy.maze.dto.Maze;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KruskalGeneratorTest {

    private KruskalGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new KruskalGenerator();
    }

    @Test
    void shouldGenerateMazeWithCorrectDimensions() {
        int width = 11;
        int height = 11;
        Maze maze = generator.generate(width, height);
        assertNotNull(maze, "Лабиринт null");
        assertEquals(height, maze.cells().length - 2, "Высота неверна");
        assertEquals(width, maze.cells()[0].length - 2, "Ширина неверна");
    }

    @Test
    void shouldGenerateValidMazeStructure() {
        int size = 11;
        Maze maze = generator.generate(size, size);
        assertTrue(hasValidBorders(maze), "Нет границ");
        assertTrue(hasEnoughPaths(maze), "Мало путей");
        assertTrue(isConnected(maze), "Не связный");
    }

    @Test
    void shouldGenerateDifferentMazesOnMultipleCalls() {
        int size = 11;
        Maze maze1 = generator.generate(size, size);
        Maze maze2 = generator.generate(size, size);
        assertTrue(areMazesDifferent(maze1, maze2), "Структура одинакова");
    }

    @Test
    void shouldContainSlowTerrainTypes() {
        int size = 11;
        Maze maze = generator.generate(size, size);
        assertTrue(containsSlowTerrain(maze), "Нет замедляющих поверхностей");
    }

    @Test
    void shouldGenerateMazeWithExtraEdges() {
        int size = 11;
        Maze maze = generator.generate(size, size);
        assertTrue(hasExtraEdges(maze), "Нет дополнительных ребер");
    }

    @Test
    void shouldGenerateMazeWithRandomPassages() {
        int size = 15;
        Maze maze = generator.generate(size, size);
        assertTrue(hasRandomPassages(maze), "Нет случайных проходов");
    }

    @Test
    void shouldHandleMinimumValidSize() {
        int minSize = 5;
        Maze maze = generator.generate(minSize, minSize);
        assertNotNull(maze, "Лабиринт null");
        assertEquals(minSize, maze.cells().length - 2, "Высота неверна");
        assertEquals(minSize, maze.cells()[0].length - 2, "Ширина неверна");
    }

    private boolean hasValidBorders(Maze maze) {
        CellType[][] cells = maze.cells();
        int height = cells.length;
        int width = cells[0].length;
        for (int x = 0; x < width; x++) {
            if (cells[0][x] != CellType.WALL || cells[height - 1][x] != CellType.WALL) {
                return false;
            }
        }
        for (CellType[] cell : cells) {
            if (cell[0] != CellType.WALL || cell[width - 1] != CellType.WALL) {
                return false;
            }
        }
        return true;
    }

    private boolean hasEnoughPaths(Maze maze) {
        CellType[][] cells = maze.cells();
        int pathC = 0;
        int totalCells = cells.length * cells[0].length;
        for (CellType[] row : cells) {
            for (CellType cell : row) {
                if (isTraversable(cell)) {
                    pathC++;
                }
            }
        }
        return pathC > totalCells * 0.25;
    }

    private boolean isConnected(Maze maze) {
        CellType[][] cells = maze.cells();
        int height = cells.length;
        int width = cells[0].length;
        boolean[][] visited = new boolean[height][width];
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (isTraversable(cells[y][x])) {
                    dfsConnectivityCheck(cells, visited, x, y);
                    return checkAllTraversableVisited(cells, visited);
                }
            }
        }
        return false;
    }

    private void dfsConnectivityCheck(CellType[][] cells, boolean[][] visited, int x, int y) {
        if (x <= 0
                || x >= cells[0].length - 1
                || y <= 0
                || y >= cells.length - 1
                || visited[y][x]
                || !isTraversable(cells[y][x])) {
            return;
        }
        visited[y][x] = true;
        dfsConnectivityCheck(cells, visited, x + 1, y);
        dfsConnectivityCheck(cells, visited, x - 1, y);
        dfsConnectivityCheck(cells, visited, x, y + 1);
        dfsConnectivityCheck(cells, visited, x, y - 1);
    }

    private boolean checkAllTraversableVisited(CellType[][] cells, boolean[][] visited) {
        for (int y = 1; y < cells.length - 1; y++) {
            for (int x = 1; x < cells[0].length - 1; x++) {
                if (isTraversable(cells[y][x]) && !visited[y][x]) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isTraversable(CellType cell) {
        return cell == CellType.PATH || cell == CellType.MUD || cell == CellType.SWAMP;
    }

    private boolean areMazesDifferent(Maze maze1, Maze maze2) {
        CellType[][] cells1 = maze1.cells();
        CellType[][] cells2 = maze2.cells();
        int diff = 0;
        for (int y = 0; y < cells1.length; y++) {
            for (int x = 0; x < cells1[0].length; x++) {
                if (cells1[y][x] != cells2[y][x]) {
                    diff++;
                }
            }
        }
        return diff > (cells1.length * cells1[0].length) * 0.1;
    }

    private boolean containsSlowTerrain(Maze maze) {
        CellType[][] cells = maze.cells();
        for (CellType[] row : cells) {
            for (CellType cell : row) {
                if (cell == CellType.MUD || cell == CellType.SWAMP) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasExtraEdges(Maze maze) {
        CellType[][] cells = maze.cells();
        int height = cells.length;
        int width = cells[0].length;
        int horizontalEdges = 0;
        int verticalEdges = 0;
        for (int y = 1; y < height - 1; y += 2) {
            for (int x = 2; x < width - 1; x += 2) {
                if (cells[y][x] == CellType.PATH) {
                    horizontalEdges++;
                }
            }
        }
        for (int y = 2; y < height - 1; y += 2) {
            for (int x = 1; x < width - 1; x += 2) {
                if (cells[y][x] == CellType.PATH) {
                    verticalEdges++;
                }
            }
        }
        return horizontalEdges > 0 && verticalEdges > 0;
    }

    private boolean hasRandomPassages(Maze maze) {
        CellType[][] cells = maze.cells();
        int height = cells.length;
        int width = cells[0].length;
        int randomPassages = 0;
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (cells[y][x] == CellType.PATH) {
                    if ((x % 2 == 0 && y % 2 == 1) || (x % 2 == 1 && y % 2 == 0)) {
                        randomPassages++;
                    }
                }
            }
        }
        return randomPassages > 0;
    }
}
