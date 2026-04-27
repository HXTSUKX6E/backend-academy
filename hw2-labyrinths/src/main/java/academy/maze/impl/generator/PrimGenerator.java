package academy.maze.impl.generator;

import academy.maze.dto.CellType;
import academy.maze.utils.MazeUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PrimGenerator extends BaseGenerator {
    private final Random random = new Random();

    /**
     * Направления для проверки соседних клеток. Каждый массив содержит [dx, dy, type]: - dx, dy: смещение координат -
     * type: 1 - горизонтальное направление, 2 - вертикальное направление
     */
    private final int[][] directions = {{0, -1, 2}, {0, 1, 2}, {-1, 0, 1}, {1, 0, 1}};

    @Override
    protected void generateMaze(CellType[][] cells) {
        int height = cells.length;
        int width = cells[0].length;

        MazeUtils.initializeAllWalls(cells);

        int startX = 1;
        int startY = 1;
        cells[startY][startX] = CellType.PATH;

        List<int[]> walls = new ArrayList<>();
        addAdjacentWalls(startX, startY, cells, walls);

        while (!walls.isEmpty()) {
            int[] wall = walls.remove(random.nextInt(walls.size()));
            int wallX = wall[0];
            int wallY = wall[1];

            int dx = 0, dy = 0;
            if (wallX % 2 == 0) dx = (wall[2] == 1 ? 1 : -1);
            else dy = (wall[2] == 2 ? 1 : -1);

            int nextX = wallX + dx;
            int nextY = wallY + dy;

            if (isValidCell(nextX, nextY, width, height) && cells[nextY][nextX] == CellType.WALL) {
                cells[wallY][wallX] = CellType.PATH;
                cells[nextY][nextX] = CellType.PATH;
                addAdjacentWalls(nextX, nextY, cells, walls);
            }
        }

        MazeUtils.addSlowTerrain(cells, random);
        addSlowTerrainInLongCorridors(cells);
    }

    private void addAdjacentWalls(int x, int y, CellType[][] cells, List<int[]> walls) {
        int height = cells.length;
        int width = cells[0].length;

        for (int[] dir : directions) {
            int wallX = x + dir[0];
            int wallY = y + dir[1];
            int nextX = x + dir[0] * 2;
            int nextY = y + dir[1] * 2;

            if (isValidCell(nextX, nextY, width, height) && cells[nextY][nextX] == CellType.WALL) {
                walls.add(new int[] {wallX, wallY, dir[2]});
            }
        }
    }

    private boolean isValidCell(int x, int y, int width, int height) {
        return x > 0 && x < width - 1 && y > 0 && y < height - 1;
    }

    private void addSlowTerrainInLongCorridors(CellType[][] cells) {
        int height = cells.length;
        int width = cells[0].length;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (cells[y][x] == CellType.PATH && isInLongCorridor(cells, x, y) && random.nextDouble() < 0.4) {
                    cells[y][x] = CellType.MUD;
                }
            }
        }
    }

    private boolean isInLongCorridor(CellType[][] cells, int x, int y) {
        int horizontalLength = countCorridorLength(cells, x, y, 1, 0) + countCorridorLength(cells, x, y, -1, 0) - 1;
        int verticalLength = countCorridorLength(cells, x, y, 0, 1) + countCorridorLength(cells, x, y, 0, -1) - 1;

        return horizontalLength >= 4 || verticalLength >= 4;
    }

    private int countCorridorLength(CellType[][] cells, int startX, int startY, int dx, int dy) {
        int length = 0;
        int x = startX;
        int y = startY;

        while (isValidCell(x, y, cells[0].length, cells.length) && cells[y][x] != CellType.WALL) {
            length++;
            x += dx;
            y += dy;

            if (MazeUtils.countNeighborPassages(cells, x, y) > 2) {
                break;
            }
        }
        return length;
    }
}
