package academy.maze.dto;

/** Тип ячейки в лабиринте. WALL - стена, PATH - свободная ячейка. */
public enum CellType {
    WALL(Integer.MAX_VALUE),
    PATH(1),
    MUD(2),
    SWAMP(3);

    private final int moveCost;

    CellType(int moveCost) {
        this.moveCost = moveCost;
    }

    public int getMoveCost() {
        return moveCost;
    }
}
