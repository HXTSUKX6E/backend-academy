package academy.maze;

import academy.maze.dto.Maze;

/** Генератор лабиринта */
public interface Generator {

    /**
     * Генерирует лабиринт.
     *
     * @param width ширина лабиринта.
     * @param height высота лабиринта.
     * @return лабиринт
     * @throws IllegalArgumentException если невозможно сгенерировать лабиринт.
     */
    Maze generate(int width, int height);
}
