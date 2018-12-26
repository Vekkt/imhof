package ch.epfl.imhof.geometry;

import java.util.List;

public final class OpenPolyLine extends PolyLine {
    private List<Point> points;

    public OpenPolyLine(List<Point> points) {
        super(points);
    }

    @Override
    public final Boolean isClosed() {
        return false;
    }
}
