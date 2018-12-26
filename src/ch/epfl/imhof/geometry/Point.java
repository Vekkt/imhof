package ch.epfl.imhof.geometry;

import ch.epfl.imhof.Preconditions;

import java.util.function.Function;

public final class Point {
    private final double x;
    private final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public final double x() {
        return this.x;
    }

    public final double y() {
        return this.y;
    }

    public static Function<Point, Point> alignedCoordinateChange(Point b1, Point r1, Point b2, Point r2) {
        Preconditions.checkArgument(!(b1.x == b2.x || b1.y == b2.y || r1.x == r2.x || r1.y == r2.y));

        double homothetyX = (r2.x - r1.x) / (b2.x - b1.x);
        double homothetyY = (r2.y - r1.y) / (b2.y - b1.y);
        double translationX = (r1.x - (homothetyX * b1.x));
        double translationY = (r1.y - (homothetyY * b1.y));

        return p -> new Point(p.x * homothetyX + translationX, p.y * homothetyY + translationY);
    }


}
