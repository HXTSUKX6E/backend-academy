package academy.maze.impl.solver;

import academy.maze.dto.Maze;
import academy.maze.dto.Path;
import academy.maze.dto.Point;
import java.util.*;

public class BFSSolver extends AbstractSolver {

    @Override
    public Path solve(Maze maze, Point start, Point end) {
        if (start.equals(end)) {
            return new Path(new Point[] {start});
        }

        int height = maze.cells().length;
        int width = maze.cells()[0].length;

        Point[][] prev = new Point[height][width];
        boolean[][] visited = new boolean[height][width];

        Queue<Point> queue = new LinkedList<>();
        queue.add(start);
        visited[start.y()][start.x()] = true;

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            int cx = current.x();
            int cy = current.y();

            if (current.equals(end)) break;

            for (int i = 0; i < DX.length; i++) {
                int nx = cx + DX[i];
                int ny = cy + DY[i];

                if (!isValidPoint(maze, nx, ny) || visited[ny][nx]) continue;

                visited[ny][nx] = true;
                prev[ny][nx] = current;
                queue.add(new Point(nx, ny));
            }
        }

        return reconstructPath(prev, end);
    }
}
