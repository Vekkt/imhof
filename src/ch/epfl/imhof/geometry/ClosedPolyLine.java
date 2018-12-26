package ch.epfl.imhof.geometry;

import java.util.List;

public final class ClosedPolyLine extends PolyLine {
    public ClosedPolyLine(List<Point> points) {
        super(points);
    }

    @Override
    public final boolean isClosed() {
        return true;
    }

    public final double area() {
        double x, y1, y2;
        double area = 0;

        for (int i = 0; i < points().size(); i++) {
            x = getPoint(i).x();
            y1 = getPoint(i-1).y();
            y2 = getPoint(i+1).y();

            area += x * (y2 - y1);
        }

        return Math.abs(area) / 2d;
    }

    public final boolean containsPoint(Point p) {
        int index = 0;
        for (int i = 0; i < points().size(); i++) {
            Point p1 = getPoint(i);
            Point p2 = getPoint(i + 1);
            if (p1.y() <= p.y()) {
                if (p2.y() > p.y() && isAtLeft(p1, p2, p))
                    index++;
            } else {
                if (p2.y() <= p.y() && isAtLeft(p2, p1, p))
                    index--;
            }
        }
        return index != 0;
    }

    private boolean isAtLeft(Point p1, Point p2, Point p) {
        return (p1.x() - p.x()) * (p2.y() - p.y()) > (p2.x() - p.x()) * (p1.y() - p.y());
    }

    private Point getPoint(int index) {
        return this.points().get(Math.floorMod(index, this.points().size()));
    }
}
