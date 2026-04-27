package academy.maze.utils;

import academy.maze.dto.Point;

public class PointUtils {

    public static Point parsePoint(String pointStr) {
        String[] coords = pointStr.split(",");
        if (coords.length != 2) {
            throw new IllegalArgumentException("Point must be in format: x,y");
        }
        try {
            return new Point(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Point coordinates must be integers: " + pointStr);
        }
    }
}
