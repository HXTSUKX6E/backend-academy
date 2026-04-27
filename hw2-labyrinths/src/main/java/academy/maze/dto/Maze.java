package academy.maze.dto;

/**
 * Лабиринт.
 *
 * @param cells Массив ячеек лабиринта.
 */
public record Maze(CellType[][] cells) {}
