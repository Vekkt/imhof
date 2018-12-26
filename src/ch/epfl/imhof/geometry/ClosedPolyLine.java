package ch.epfl.imhof.geometry;

import java.util.List;

public final class ClosedPolyLine extends PolyLine {
    public ClosedPolyLine(List<Point> points) {
        super(points);
    }

    @Override
    public final Boolean isClosed() {
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

        return Math.abs(area) / 2.0;
    }

    public final boolean containsPoint(Point p) {
        int index = 0;
        Point p1, p2;

        for (int i = 0; i < points().size(); i++) {
            p1 = getPoint(i);
            p2 = getPoint(i + 1);


            if (p1.y() <= p.y())
                if (p2.y() > p.y() && isAtLeft(p, p1, p2))
                    index += 1;
            else
                if (p2.y() <= p.y() && isAtLeft(p, p2, p1))
                    index -= 1;
        }

        return index != 0;
    }

    private boolean isAtLeft(Point p, Point p1, Point p2) {
        return (p1.x() - p.x()) * (p2.y() - p.y()) >= (p2.x() - p.x()) * (p1.y() - p.y());
    }

    private Point getPoint(int index) {
        return this.points().get(Math.floorMod(index, this.points().size()));
    }
}
