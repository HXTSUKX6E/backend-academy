package academy.maze.impl.solver;

import academy.maze.dto.Maze;
import academy.maze.dto.Path;
import academy.maze.dto.Point;
import java.util.*;

public class DijkstraSolver extends AbstractSolver {

    @Override
    public Path solve(Maze maze, Point start, Point end) {
        if (end.equals(start)) {
            return new Path(new Point[] {start});
        }

        int height = maze.cells().length;
        int width = maze.cells()[0].length;

        int[][] dist = new int[height][width];
        Point[][] prev = new Point[height][width];
        boolean[][] visited = new boolean[height][width];

        for (int y = 0; y < height; y++) {
            Arrays.fill(dist[y], Integer.MAX_VALUE);
        }
        dist[start.y()][start.x()] = 0;

        PriorityQueue<Point> pq = new PriorityQueue<>(Comparator.comparingInt(p -> dist[p.y()][p.x()]));
        pq.add(start);

        while (!pq.isEmpty()) {
            Point current = pq.poll();
            int cx = current.x();
            int cy = current.y();

            if (visited[cy][cx]) continue;
            visited[cy][cx] = true;

            if (current.equals(end)) break;

            for (int i = 0; i < 4; i++) {
                int nx = cx + DX[i];
                int ny = cy + DY[i];

                if (!isValidPoint(maze, nx, ny)) continue;
                if (visited[ny][nx]) continue;

                int moveCost = getMoveCost(maze.cells()[ny][nx]);
                int newDist = dist[cy][cx] + moveCost;

                if (newDist < dist[ny][nx]) {
                    dist[ny][nx] = newDist;
                    prev[ny][nx] = current;
                    pq.add(new Point(nx, ny));
                }
            }
        }

        return reconstructPath(prev, end);
    }
}
