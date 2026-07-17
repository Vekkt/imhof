package ch.epfl.imhof.contours;

import ch.epfl.imhof.Preconditions;
import ch.epfl.imhof.geometry.Point;
import ch.epfl.imhof.geometry.PolyLine;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public final class Chaiken {
    private Chaiken() {
    }

    public static List<Point> smooth(PolyLine polyLine, int iterations) {
        requireNonNull(polyLine);
        return smooth(polyLine.points(), polyLine.isClosed(), iterations);
    }

    public static List<Point> smooth(List<Point> points, boolean closed, int iterations) {
        requireNonNull(points);
        Preconditions.checkArgument(iterations >= 0, "Iterations must be non-negative.");

        List<Point> smoothed = List.copyOf(points);
        for (int i = 0; i < iterations; i++) {
            smoothed = smoothOnce(smoothed, closed);
        }
        return smoothed;
    }

    private static List<Point> smoothOnce(List<Point> points, boolean closed) {
        if (points.size() <= 1) {
            return List.copyOf(points);
        }

        return closed ? smoothClosed(points) : smoothOpen(points);
    }

    private static List<Point> smoothClosed(List<Point> points) {
        List<Point> smoothed = new ArrayList<>(points.size() * 2);

        for (int i = 0; i < points.size(); i++) {
            addCutPoints(smoothed, points.get(i), points.get((i + 1) % points.size()));
        }

        return List.copyOf(smoothed);
    }

    private static List<Point> smoothOpen(List<Point> points) {
        List<Point> smoothed = new ArrayList<>(points.size() * 2);
        smoothed.add(points.getFirst());

        for (int i = 0; i < points.size() - 1; i++) {
            addCutPoints(smoothed, points.get(i), points.get(i + 1));
        }

        smoothed.add(points.getLast());
        return List.copyOf(smoothed);
    }

    private static void addCutPoints(List<Point> points, Point p0, Point p1) {
        points.add(interpolate(p0, p1, 0.25d));
        points.add(interpolate(p0, p1, 0.75d));
    }

    private static Point interpolate(Point p0, Point p1, double t) {
        return new Point(
                (1 - t) * p0.x() + t * p1.x(),
                (1 - t) * p0.y() + t * p1.y());
    }
}
