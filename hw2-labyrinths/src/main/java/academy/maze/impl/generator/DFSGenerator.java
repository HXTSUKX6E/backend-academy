package academy.maze.impl.generator;

import academy.maze.dto.CellType;
import academy.maze.utils.MazeUtils;
import java.util.*;

public class DFSGenerator extends BaseGenerator {
    private final Random random = new Random();

    @Override
    protected void generateMaze(CellType[][] cells) {
        int height = cells.length;
        int width = cells[0].length;

        initializePathGrid(cells);

        boolean[][] visited = new boolean[height][width];
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[] {1, 1});
        visited[1][1] = true;

        while (!stack.isEmpty()) {
            MazeUtils.processNextCell(visited, stack, cells, random);

            if (stack.isEmpty()) {
                MazeUtils.addNewStartPoint(cells, visited, stack);
            }
        }

        MazeUtils.ensureAllCellsConnected(cells);
        MazeUtils.addSlowTerrain(cells, random);
    }

    private void initializePathGrid(CellType[][] cells) {
        int height = cells.length;
        int width = cells[0].length;

        for (int y = 1; y < height - 1; y += 2) {
            for (int x = 1; x < width - 1; x += 2) {
                cells[y][x] = CellType.PATH;
            }
        }
    }
}
