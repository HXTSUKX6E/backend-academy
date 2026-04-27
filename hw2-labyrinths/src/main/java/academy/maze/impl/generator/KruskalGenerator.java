package academy.maze.impl.generator;

import academy.maze.dto.CellType;
import academy.maze.utils.MazeUtils;
import academy.maze.utils.UnionFind;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class KruskalGenerator extends BaseGenerator {
    private final Random random = new Random();

    @Override
    protected void generateMaze(CellType[][] cells) {
        int height = cells.length;
        int width = cells[0].length;

        MazeUtils.initializeAllWalls(cells);

        List<Edge> edges = new ArrayList<>();
        for (int y = 1; y < height - 1; y += 2) {
            for (int x = 1; x < width - 1; x += 2) {
                cells[y][x] = CellType.PATH;

                if (x < width - 2) edges.add(new Edge(x, y, x + 2, y));
                if (y < height - 2) edges.add(new Edge(x, y, x, y + 2));
            }
        }

        Collections.shuffle(edges, random);

        UnionFind uf = new UnionFind(width / 2 * (height / 2));

        for (Edge edge : edges) {
            int fromId = edge.y1 / 2 * (width / 2) + edge.x1 / 2;
            int toId = edge.y2 / 2 * (width / 2) + (edge.x2 / 2);

            if (uf.find(fromId) != uf.find(toId)) {
                uf.union(fromId, toId);
                int wallX = edge.x1 + (edge.x2 - edge.x1) / 2;
                int wallY = edge.y1 + (edge.y2 - edge.y1) / 2;
                cells[wallY][wallX] = CellType.PATH;
            }
        }

        int extraEdges = Math.max(1, edges.size() / 5);
        Collections.shuffle(edges, random);

        for (int i = 0; i < extraEdges; i++) {
            Edge edge = edges.get(i);
            int wallX = edge.x1 + (edge.x2 - edge.x1) / 2;
            int wallY = edge.y1 + (edge.y2 - edge.y1) / 2;
            cells[wallY][wallX] = CellType.PATH;
        }

        addRandomPassages(cells);
        MazeUtils.addSlowTerrain(cells, random);
        addSlowTerrainInDeadEnds(cells);
    }

    private void addRandomPassages(CellType[][] cells) {
        int height = cells.length;
        int width = cells[0].length;

        int randomPassages = width * height / 20;

        for (int i = 0; i < randomPassages; i++) {
            int x = 1 + random.nextInt(width - 2);
            int y = 1 + random.nextInt(height - 2);
            if (cells[y][x] == CellType.WALL && !isBorder(cells, x, y)) {
                cells[y][x] = CellType.PATH;
            }
        }
    }

    private void addSlowTerrainInDeadEnds(CellType[][] cells) {
        int height = cells.length;
        int width = cells[0].length;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (cells[y][x] == CellType.PATH && MazeUtils.isDeadEnd(cells, x, y)) {
                    if (random.nextBoolean() && random.nextBoolean()) {
                        cells[y][x] = CellType.MUD;
                    } else if (random.nextBoolean()) {
                        cells[y][x] = CellType.SWAMP;
                    }
                }
            }
        }
    }

    private boolean isBorder(CellType[][] cells, int x, int y) {
        return x == 0 || x == cells[0].length - 1 || y == 0 || y == cells.length - 1;
    }

    private record Edge(int x1, int y1, int x2, int y2) {}
}
