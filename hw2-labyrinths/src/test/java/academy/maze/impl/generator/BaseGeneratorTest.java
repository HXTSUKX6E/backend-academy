package academy.maze.impl.generator;

import static org.junit.jupiter.api.Assertions.*;

import academy.maze.dto.CellType;
import academy.maze.dto.Maze;
import org.junit.jupiter.api.Test;

class BaseGeneratorTest {

    private static class TestGenerator extends BaseGenerator {
        @Override
        protected void generateMaze(CellType[][] cells) {
            int height = cells.length;
            int width = cells[0].length;

            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    cells[y][x] = CellType.PATH;
                }
            }
        }
    }

    private final TestGenerator generator = new TestGenerator();

    @Test
    void shouldGenerateMazeWithBorders() {
        int width = 5;
        int height = 5;
        Maze maze = generator.generate(width, height);
        assertNotNull(maze, "Лабиринт null");
        assertEquals(height + 2, maze.cells().length, "Высота неверна");
        assertEquals(width + 2, maze.cells()[0].length, "Ширина неверна");
    }

    @Test
    void shouldHaveWallBorders() {
        int width = 5;
        int height = 5;
        Maze maze = generator.generate(width, height);
        CellType[][] cells = maze.cells();
        for (int x = 0; x < width + 2; x++) {
            assertEquals(CellType.WALL, cells[0][x], "Верхняя граница не стена");
            assertEquals(CellType.WALL, cells[height + 1][x], "Нижняя граница не стена");
        }
        for (int y = 0; y < height + 2; y++) {
            assertEquals(CellType.WALL, cells[y][0], "Левая граница не стена");
            assertEquals(CellType.WALL, cells[y][width + 1], "Правая граница не стена");
        }
    }

    @Test
    void shouldHavePathsInside() {
        int width = 5;
        int height = 5;
        Maze maze = generator.generate(width, height);
        CellType[][] cells = maze.cells();
        for (int y = 1; y < height + 1; y++) {
            for (int x = 1; x < width + 1; x++) {
                assertEquals(CellType.PATH, cells[y][x], "Внутренняя клетка не путь");
            }
        }
    }

    @Test
    void shouldThrowExceptionForInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate(0, 5), "Не выброшено для ширины 0");
        assertThrows(IllegalArgumentException.class, () -> generator.generate(5, 0), "Не выброшено для высоты 0");
        assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate(-1, 5),
                "Не выброшено для отрицательной ширины");
        assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate(5, -1),
                "Не выброшено для отрицательной высоты");
    }

    @Test
    void shouldHandleMinimumDimensions() {
        int width = 1;
        int height = 1;

        Maze maze = generator.generate(width, height);

        assertNotNull(maze, "Лабиринт null");
        assertEquals(height + 2, maze.cells().length, "Высота неверна");
        assertEquals(width + 2, maze.cells()[0].length, "Ширина неверна");
        CellType[][] cells = maze.cells();
        assertEquals(CellType.WALL, cells[0][0], "Угол не стена");
        assertEquals(CellType.WALL, cells[0][1], "Верх не стена");
        assertEquals(CellType.WALL, cells[0][2], "Угол не стена");
        assertEquals(CellType.WALL, cells[1][0], "Бок не стена");
        assertEquals(CellType.PATH, cells[1][1], "Центр не путь");
        assertEquals(CellType.WALL, cells[1][2], "Бок не стена");
        assertEquals(CellType.WALL, cells[2][0], "Угол не стена");
        assertEquals(CellType.WALL, cells[2][1], "Низ не стена");
        assertEquals(CellType.WALL, cells[2][2], "Угол не стена");
    }

    @Test
    void shouldCreateEmptyMazeWithWalls() {
        BaseGenerator baseGenerator = new TestGenerator();
        int width = 3;
        int height = 3;
        CellType[][] cells = baseGenerator.createEmptyMaze(width, height);
        assertNotNull(cells, "Массив null");
        assertEquals(height, cells.length, "Высота неверна");
        assertEquals(width, cells[0].length, "Ширина неверна");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                assertEquals(CellType.WALL, cells[y][x], "Клетка не стена");
            }
        }
    }
}
