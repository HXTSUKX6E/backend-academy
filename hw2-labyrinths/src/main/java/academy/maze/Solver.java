package academy.maze;

import academy.maze.dto.Maze;
import academy.maze.dto.Path;
import academy.maze.dto.Point;

/** Решатель лабиринта */
public interface Solver {

    /**
     * Решение лабиринта. Если путь не найден, то возвращается путь с длиной 0.
     *
     * @param maze лабиринт.
     * @param start начальная точка.
     * @param end конечная точка.
     * @return путь в лабиринте.
     */
    Path solve(Maze maze, Point start, Point end);
}
