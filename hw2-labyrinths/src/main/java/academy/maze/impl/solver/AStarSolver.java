package academy.maze.impl.solver;

import academy.maze.dto.Maze;
import academy.maze.dto.Path;
import academy.maze.dto.Point;
import java.util.*;

public class AStarSolver extends AbstractSolver {

    @Override
    public Path solve(Maze maze, Point start, Point end) {
        if (start.equals(end)) {
            return new Path(new Point[] {start});
        }

        int height = maze.cells().length;
        int width = maze.cells()[0].length;

        int[][] gScore = new int[height][width];
        int[][] fScore = new int[height][width];
        Point[][] cameFrom = new Point[height][width];

        for (int y = 0; y < height; y++) {
            Arrays.fill(gScore[y], Integer.MAX_VALUE);
            Arrays.fill(fScore[y], Integer.MAX_VALUE);
        }

        gScore[start.y()][start.x()] = 0;
        fScore[start.y()][start.x()] = heuristic(start, end);

        PriorityQueue<Point> openSet = new PriorityQueue<>(Comparator.comparingInt(p -> fScore[p.y()][p.x()]));
        openSet.add(start);

        while (!openSet.isEmpty()) {
            Point current = openSet.poll();
            int cx = current.x();
            int cy = current.y();

            if (current.equals(end)) {
                break;
            }

            for (int i = 0; i < DX.length; i++) {
                int nx = cx + DX[i];
                int ny = cy + DY[i];

                if (!isValidPoint(maze, nx, ny)) continue;

                int moveCost = getMoveCost(maze.cells()[ny][nx]);
                int tentativeGScore = gScore[cy][cx] + moveCost;

                if (tentativeGScore < gScore[ny][nx]) {
                    cameFrom[ny][nx] = current;
                    gScore[ny][nx] = tentativeGScore;
                    fScore[ny][nx] = tentativeGScore + heuristic(new Point(nx, ny), end);

                    if (!openSet.contains(new Point(nx, ny))) {
                        openSet.add(new Point(nx, ny));
                    }
                }
            }
        }

        return reconstructPath(cameFrom, end);
    }

    private int heuristic(Point a, Point b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
    }
}
