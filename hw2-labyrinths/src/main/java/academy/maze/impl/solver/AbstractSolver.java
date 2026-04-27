package academy.maze.impl.solver;

import academy.maze.Solver;
import academy.maze.dto.CellType;
import academy.maze.dto.Maze;
import academy.maze.dto.Path;
import academy.maze.dto.Point;
import java.util.*;

public abstract class AbstractSolver implements Solver {

    static final int[] DX = {0, 0, -1, 1};
    static final int[] DY = {-1, 1, 0, 0};

    protected int getMoveCost(CellType cellType) {
        return cellType.getMoveCost();
    }

    protected Path reconstructPath(Point[][] prev, Point end) {
        List<Point> path = new ArrayList<>();
        Point p = end;

        if (prev[p.y()][p.x()] != null) {
            while (p != null) {
                path.add(p);
                p = prev[p.y()][p.x()];
            }
            Collections.reverse(path);
        }

        return new Path(path.toArray(new Point[0]));
    }

    protected boolean isValidPoint(Maze maze, int x, int y) {
        return x >= 0
                && x < maze.cells()[0].length
                && y >= 0
                && y < maze.cells().length
                && maze.cells()[y][x] != CellType.WALL;
    }
}
