package academy.maze.display;

import academy.maze.display.strategy.AsciiPrintStrategy;
import academy.maze.display.strategy.CellPrintStrategy;
import academy.maze.display.strategy.UnicodePrintStrategy;
import academy.maze.dto.CellType;
import academy.maze.dto.Maze;
import academy.maze.dto.Path;
import academy.maze.dto.Point;

public class MazeFormatter {
    private final CellPrintStrategy asciiStrategy;
    private final CellPrintStrategy unicodeStrategy;

    public MazeFormatter() {
        this(new AsciiPrintStrategy(), new UnicodePrintStrategy());
    }

    public MazeFormatter(CellPrintStrategy asciiStrategy, CellPrintStrategy unicodeStrategy) {
        this.asciiStrategy = asciiStrategy;
        this.unicodeStrategy = unicodeStrategy;
    }

    public String formatBasicMaze(Maze maze) {
        return formatMazeInternal(maze, asciiStrategy);
    }

    public String formatMazeWithUnicode(Maze maze) {
        return formatMazeInternal(maze, unicodeStrategy);
    }

    public String formatMazeWithSolution(Maze maze, Path path) {
        return formatMazeWithPathInternal(maze, path, asciiStrategy, true);
    }

    public String formatMazeWithPathForConsole(Maze maze, Path path) {
        return formatMazeWithPathInternal(maze, path, asciiStrategy, false);
    }

    public String formatMazeWithPathAndUnicodeForConsole(Maze maze, Path path) {
        return formatMazeWithPathInternal(maze, path, unicodeStrategy, false);
    }

    private String formatMazeInternal(Maze maze, CellPrintStrategy strategy) {
        StringBuilder sb = new StringBuilder();
        CellType[][] cells = maze.cells();

        for (CellType[] row : cells) {
            for (CellType cell : row) {
                sb.append(strategy.getSymbol(cell));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String formatMazeWithPathInternal(Maze maze, Path path, CellPrintStrategy strategy, boolean forFile) {
        StringBuilder sb = new StringBuilder();
        CellType[][] cells = maze.cells();
        Point[] pathPoints = path.points();

        ColorCodes colors = createColorCodes(forFile);
        boolean[][] pathMap = createPathMap(cells, pathPoints);
        PathEndpoints endpoints = new PathEndpoints(pathPoints);

        for (int y = 0; y < cells.length; y++) {
            appendRow(sb, cells, pathMap, endpoints, y, strategy, forFile, colors);
            sb.append("\n");
        }
        return sb.toString();
    }

    private ColorCodes createColorCodes(boolean forFile) {
        return new ColorCodes(
                forFile ? "" : "\u001B[31m",
                forFile ? "" : "\u001B[33m",
                forFile ? "" : "\u001B[34m",
                forFile ? "" : "\u001B[0m",
                forFile ? "" : "\u001B[42m");
    }

    private boolean[][] createPathMap(CellType[][] cells, Point[] pathPoints) {
        boolean[][] pathMap = new boolean[cells.length][cells[0].length];
        for (Point p : pathPoints) {
            pathMap[p.y()][p.x()] = true;
        }
        return pathMap;
    }

    private void appendRow(
            StringBuilder sb,
            CellType[][] cells,
            boolean[][] pathMap,
            PathEndpoints endpoints,
            int y,
            CellPrintStrategy strategy,
            boolean forFile,
            ColorCodes colors) {
        for (int x = 0; x < cells[y].length; x++) {
            String symbol = getCellSymbol(cells, pathMap, endpoints, x, y, strategy, forFile, colors);
            sb.append(symbol);
        }
    }

    private String getCellSymbol(
            CellType[][] cells,
            boolean[][] pathMap,
            PathEndpoints endpoints,
            int x,
            int y,
            CellPrintStrategy strategy,
            boolean forFile,
            ColorCodes colors) {
        if (endpoints.isStart(x, y)) {
            return getStartSymbol(strategy, forFile, colors);
        }
        if (endpoints.isEnd(x, y)) {
            return getEndSymbol(strategy, forFile, colors);
        }
        if (pathMap[y][x]) {
            return getPathSymbol(cells[y][x], strategy, forFile, colors);
        }
        return getTerrainSymbol(cells[y][x], strategy, forFile, colors);
    }

    private String getStartSymbol(CellPrintStrategy strategy, boolean forFile, ColorCodes colors) {
        return forFile ? "O" : strategy instanceof UnicodePrintStrategy ? colors.bgGreen + "ST" + colors.reset : "S";
    }

    private String getEndSymbol(CellPrintStrategy strategy, boolean forFile, ColorCodes colors) {
        return forFile ? "X" : strategy instanceof UnicodePrintStrategy ? colors.bgGreen + "FI" + colors.reset : "F";
    }

    private String getPathSymbol(CellType cell, CellPrintStrategy strategy, boolean forFile, ColorCodes colors) {
        if (forFile) return ".";
        if (strategy instanceof AsciiPrintStrategy) return colors.red + "." + colors.reset;

        return switch (cell) {
            case MUD -> colors.red + "░░" + colors.reset;
            case SWAMP -> colors.red + "▓▓" + colors.reset;
            default -> colors.red + "██" + colors.reset;
        };
    }

    private String getTerrainSymbol(CellType cell, CellPrintStrategy strategy, boolean forFile, ColorCodes colors) {
        if (strategy instanceof UnicodePrintStrategy) {
            return getUnicodeTerrainSymbol(cell, forFile, colors);
        }
        return getAsciiTerrainSymbol(cell, forFile, colors);
    }

    private String getUnicodeTerrainSymbol(CellType cell, boolean forFile, ColorCodes colors) {
        return switch (cell) {
            case WALL -> "██";
            case MUD -> forFile ? "░░" : colors.yellow + "░░" + colors.reset;
            case SWAMP -> forFile ? "▓▓" : colors.blue + "▓▓" + colors.reset;
            default -> "  ";
        };
    }

    private String getAsciiTerrainSymbol(CellType cell, boolean forFile, ColorCodes colors) {
        return switch (cell) {
            case WALL -> "#";
            case MUD -> forFile ? ":" : colors.yellow + ":" + colors.reset;
            case SWAMP -> forFile ? "~" : colors.blue + "~" + colors.reset;
            default -> " ";
        };
    }

    private record ColorCodes(String red, String yellow, String blue, String reset, String bgGreen) {}

    private record PathEndpoints(Point start, Point end) {
        PathEndpoints(Point[] pathPoints) {
            this(pathPoints[0], pathPoints[pathPoints.length - 1]);
        }

        boolean isStart(int x, int y) {
            return x == start.x() && y == start.y();
        }

        boolean isEnd(int x, int y) {
            return x == end.x() && y == end.y();
        }
    }
}
