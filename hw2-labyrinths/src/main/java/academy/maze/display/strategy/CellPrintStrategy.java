package academy.maze.display.strategy;

import academy.maze.dto.CellType;

public interface CellPrintStrategy {
    String getSymbol(CellType cellType);
}
