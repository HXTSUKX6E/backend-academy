package academy.maze.impl.generator;

import academy.maze.Generator;
import academy.maze.dto.CellType;
import academy.maze.dto.Maze;

public abstract class BaseGenerator implements Generator {

    @Override
    public final Maze generate(int width, int height) {
        validateDimensions(width, height);
        int actualWidth = calculateActualWidth(width);
        int actualHeight = calculateActualHeight(height);
        CellType[][] cells = createEmptyMaze(actualWidth, actualHeight);
        generateMaze(cells);
        return new Maze(cells);
    }

    /** Метод для реализации конкретного генератора */
    protected abstract void generateMaze(CellType[][] cells);

    protected void validateDimensions(int width, int height) {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Ширина и высота должны быть >= 1");
        }
    }

    protected int calculateActualWidth(int width) {
        return width + 2;
    }

    protected int calculateActualHeight(int height) {
        return height + 2;
    }

    protected CellType[][] createEmptyMaze(int width, int height) {
        CellType[][] cells = new CellType[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[y][x] = CellType.WALL;
            }
        }
        return cells;
    }
}
