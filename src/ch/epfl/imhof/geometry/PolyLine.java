package ch.epfl.imhof.geometry;

import ch.epfl.imhof.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class PolyLine {
    private List<Point> points;

    public PolyLine(List<Point> points) {
        Preconditions.checkArgument(!points.isEmpty());

        this.points = new ArrayList<>(points);
    }

    public static final class Builder {
        private List<Point> points = new ArrayList<>();

        public void addPoint(Point newPoint) {
            this.points.add(newPoint);
        }

        public OpenPolyLine buildOpen() {
            return new OpenPolyLine(this.points);
        }

        public ClosedPolyLine buildClosed() {
            return new ClosedPolyLine(points);
        }
    }

    public abstract boolean isClosed();

    public List<Point> points() {
        return Collections.unmodifiableList(new ArrayList<>(points));
    }

    public Point firstPoint() {
        return points.get(0);
    }
}
