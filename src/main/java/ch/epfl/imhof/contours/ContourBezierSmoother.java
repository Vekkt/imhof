package ch.epfl.imhof.contours;

import ch.epfl.imhof.geometry.BezierPolyLine;
import ch.epfl.imhof.geometry.ClosedPolyLine;
import ch.epfl.imhof.geometry.CubicBezierSegment;
import ch.epfl.imhof.geometry.Point;
import ch.epfl.imhof.geometry.PolyLine;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

final class ContourBezierSmoother {
    private static final double TENSION = 0.7;
    private static final double MAX_HANDLE_RATIO = 0.45;
    private static final double EPSILON = 1e-9;

    private ContourBezierSmoother() {}

    static PolyLine smoothClosed(List<Point> anchors) {
        List<Point> points = removeConsecutiveDuplicates(requireNonNull(anchors));
        if (points.size() < 4) {
            return new ClosedPolyLine(points);
        }

        List<CubicBezierSegment> segments = new ArrayList<>(points.size());
        for (int i = 0; i < points.size(); i++) {
            Point p0 = points.get(Math.floorMod(i - 1, points.size()));
            Point p1 = points.get(i);
            Point p2 = points.get((i + 1) % points.size());
            Point p3 = points.get((i + 2) % points.size());

            Point control1 = new Point(
                    p1.x() + (p2.x() - p0.x()) * TENSION / 6d,
                    p1.y() + (p2.y() - p0.y()) * TENSION / 6d);
            Point control2 = new Point(
                    p2.x() - (p3.x() - p1.x()) * TENSION / 6d,
                    p2.y() - (p3.y() - p1.y()) * TENSION / 6d);

            double maxHandleLength = distance(p1, p2) * MAX_HANDLE_RATIO;
            segments.add(new CubicBezierSegment(
                    p1,
                    clampHandle(p1, control1, maxHandleLength),
                    clampHandle(p2, control2, maxHandleLength),
                    p2));
        }

        return new BezierPolyLine(points, segments);
    }

    private static List<Point> removeConsecutiveDuplicates(List<Point> anchors) {
        List<Point> points = new ArrayList<>(anchors.size());
        for (Point anchor : anchors) {
            if (points.isEmpty() || distance(points.getLast(), anchor) > EPSILON) {
                points.add(anchor);
            }
        }

        while (points.size() > 1 && distance(points.getFirst(), points.getLast()) <= EPSILON) {
            points.removeLast();
        }

        if (points.isEmpty()) {
            throw new IllegalArgumentException("Cannot smooth an empty contour.");
        }
        return points;
    }

    private static Point clampHandle(Point anchor, Point control, double maxLength) {
        double dx = control.x() - anchor.x();
        double dy = control.y() - anchor.y();
        double length = Math.hypot(dx, dy);

        if (length <= EPSILON || length <= maxLength) {
            return control;
        }
        if (maxLength <= EPSILON) {
            return anchor;
        }

        double scale = maxLength / length;
        return new Point(anchor.x() + dx * scale, anchor.y() + dy * scale);
    }

    private static double distance(Point a, Point b) {
        return Math.hypot(a.x() - b.x(), a.y() - b.y());
    }
}
