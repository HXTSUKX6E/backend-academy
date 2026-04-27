package academy.maze.utils;

import academy.maze.dto.CellType;
import academy.maze.dto.Maze;
import academy.maze.dto.Point;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public class MazeUtils {

    private static final int[][] DIRS = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}};

    public static List<int[]> getUnvisitedPathNeighbors(int x, int y, boolean[][] visited, CellType[][] cells) {
        List<int[]> neighbors = new ArrayList<>();
        int height = cells.length;
        int width = cells[0].length;

        for (int[] d : DIRS) {
            int nx = x + d[0];
            int ny = y + d[1];

            if (ny >= 1
                    && ny < height - 1
                    && nx >= 1
                    && nx < width - 1
                    && !visited[ny][nx]
                    && cells[ny][nx] == CellType.PATH) {
                neighbors.add(new int[] {nx, ny});
            }
        }
        return neighbors;
    }

    public static void processNextCell(boolean[][] visited, Deque<int[]> stack, CellType[][] cells, Random random) {
        int[] current = stack.peek();
        assert current != null;
        int x = current[0];
        int y = current[1];

        List<int[]> neighbors = getUnvisitedPathNeighbors(x, y, visited, cells);

        if (!neighbors.isEmpty()) {
            int[] next = neighbors.get(random.nextInt(neighbors.size()));
            int nx = next[0];
            int ny = next[1];

            int wallX = x + (nx - x) / 2;
            int wallY = y + (ny - y) / 2;
            cells[wallY][wallX] = CellType.PATH;

            visited[ny][nx] = true;
            stack.push(next);
        } else {
            stack.pop();
        }
    }

    public static void addNewStartPoint(CellType[][] cells, boolean[][] visited, Deque<int[]> stack) {
        int height = cells.length;
        int width = cells[0].length;

        for (int y = 1; y < height - 1; y += 2) {
            for (int x = 1; x < width - 1; x += 2) {
                if (!visited[y][x] && cells[y][x] == CellType.PATH) {
                    stack.push(new int[] {x, y});
                    visited[y][x] = true;
                    return;
                }
            }
        }
    }

    public static void ensureAllCellsConnected(CellType[][] cells) {
        int height = cells.length;
        int width = cells[0].length;

        for (int y = 1; y < height - 1; y += 2) {
            for (int x = 1; x < width - 1; x += 2) {
                if (cells[y][x] == CellType.WALL) {
                    connectIsolatedCell(cells, x, y);
                }
            }
        }
    }

    public static void connectIsolatedCell(CellType[][] cells, int x, int y) {
        for (int[] d : DIRS) {
            int nx = x + d[0];
            int ny = y + d[1];

            if (ny > 0
                    && ny < cells.length - 1
                    && nx > 0
                    && nx < cells[0].length - 1
                    && cells[ny][nx] == CellType.PATH) {
                int wallX = x + (nx - x) / 2;
                int wallY = y + (ny - y) / 2;
                cells[wallY][wallX] = CellType.PATH;
                cells[y][x] = CellType.PATH;
                return;
            }
        }
    }

    public static Maze loadMazeFromFile(File file) throws IOException {

        List<String> lines = Files.readAllLines(file.toPath());
        if (lines.isEmpty()) {
            throw new IOException("File is empty: " + file.getPath());
        }

        int height = lines.size();
        int width = lines.getFirst().length();

        CellType[][] cells = new CellType[height][width];

        for (int y = 0; y < height; y++) {
            String line = lines.get(y);
            for (int x = 0; x < width; x++) {
                char c = line.charAt(x);
                cells[y][x] = parseCellType(c);
            }
        }
        return new Maze(cells);
    }

    private static CellType parseCellType(char c) {
        return switch (c) {
            case '#' -> CellType.WALL;
            case ':' -> CellType.MUD;
            case '~' -> CellType.SWAMP;
            default -> CellType.PATH;
        };
    }

    public static void validatePoint(Maze maze, Point point, String pointName) {
        int height = maze.cells().length;
        int width = maze.cells()[0].length;

        if (point.x() < 0 || point.x() >= width || point.y() < 0 || point.y() >= height) {
            throw new IllegalArgumentException(pointName + " точка " + point + " вне границ лабиринта");
        }

        if (maze.cells()[point.y()][point.x()] == CellType.WALL) {
            throw new IllegalArgumentException(pointName + " точка " + point + " находится на стене");
        }
    }

    public static void addSlowTerrain(CellType[][] cells, Random random) {
        int height = cells.length;
        int width = cells[0].length;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (cells[y][x] == CellType.PATH && random.nextDouble() < 0.15) {
                    if (random.nextDouble() < 0.6) {
                        cells[y][x] = CellType.MUD;
                    } else {
                        cells[y][x] = CellType.SWAMP;
                    }
                }
            }
        }
    }

    public static void initializeAllWalls(CellType[][] cells) {
        int height = cells.length;
        int width = cells[0].length;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[y][x] = CellType.WALL;
            }
        }
    }

    public static int countNeighborPassages(CellType[][] cells, int x, int y) {
        int height = cells.length;
        int width = cells[0].length;

        if (x <= 0 || x >= width - 1 || y <= 0 || y >= height - 1) {
            return 0;
        }

        int count = 0;
        if (cells[y - 1][x] != CellType.WALL) count++;
        if (cells[y + 1][x] != CellType.WALL) count++;
        if (cells[y][x - 1] != CellType.WALL) count++;
        if (cells[y][x + 1] != CellType.WALL) count++;

        return count;
    }

    public static boolean isDeadEnd(CellType[][] cells, int x, int y) {
        return countNeighborPassages(cells, x, y) == 1;
    }
}
