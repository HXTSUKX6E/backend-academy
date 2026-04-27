package academy.maze.display.strategy;

import academy.maze.dto.CellType;

public class AsciiPrintStrategy implements CellPrintStrategy {
    @Override
    public String getSymbol(CellType cellType) {
        return switch (cellType) {
            case WALL -> "#";
            case MUD -> ":";
            case SWAMP -> "~";
            default -> " ";
        };
    }
}
