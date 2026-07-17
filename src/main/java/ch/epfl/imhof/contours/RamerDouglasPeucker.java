package ch.epfl.imhof.contours;

import ch.epfl.imhof.Preconditions;
import ch.epfl.imhof.geometry.Point;
import ch.epfl.imhof.geometry.PolyLine;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public final class RamerDouglasPeucker {
    private RamerDouglasPeucker() {
    }

    public static List<Point> simplify(PolyLine polyLine, double tolerance) {
        requireNonNull(polyLine);
        return simplify(polyLine.points(), polyLine.isClosed(), tolerance);
    }

    public static List<Point> simplify(List<Point> points, boolean closed, double tolerance) {
        requireNonNull(points);
        Preconditions.checkArgument(tolerance >= 0, "Tolerance must be non-negative.");

        if (points.size() <= 2) {
            return List.copyOf(points);
        }

        return closed ? simplifyClosed(points, tolerance) : simplifyOpen(points, tolerance);
    }

    private static List<Point> simplifyClosed(List<Point> points, double tolerance) {
        if (points.size() <= 3) {
            return List.copyOf(points);
        }

        IndexPair split = farthestPair(points);
        List<Point> firstArc = cyclicArc(points, split.firstIndex(), split.secondIndex());
        List<Point> secondArc = cyclicArc(points, split.secondIndex(), split.firstIndex());

        List<Point> simplifiedFirstArc = simplifyOpen(firstArc, tolerance);
        List<Point> simplifiedSecondArc = simplifyOpen(secondArc, tolerance);

        List<Point> simplified = new ArrayList<>(simplifiedFirstArc);
        simplified.addAll(simplifiedSecondArc.subList(1, simplifiedSecondArc.size() - 1));

        return List.copyOf(simplified);
    }

    private static IndexPair farthestPair(List<Point> points) {
        double maxSquaredDistance = -1;
        IndexPair farthestPair = new IndexPair(0, points.size() / 2);

        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                double squaredDistance = squaredDistance(points.get(i), points.get(j));
                if (squaredDistance > maxSquaredDistance) {
                    maxSquaredDistance = squaredDistance;
                    farthestPair = new IndexPair(i, j);
                }
            }
        }

        return farthestPair;
    }

    private static List<Point> cyclicArc(List<Point> points, int fromIndex, int toIndex) {
        List<Point> arc = new ArrayList<>();
        int index = fromIndex;

        while (index != toIndex) {
            arc.add(points.get(index));
            index = Math.floorMod(index + 1, points.size());
        }
        arc.add(points.get(toIndex));

        return arc;
    }

    private static List<Point> simplifyOpen(List<Point> points, double tolerance) {
        boolean[] kept = new boolean[points.size()];
        kept[0] = true;
        kept[points.size() - 1] = true;

        keepPoints(points, 0, points.size() - 1, tolerance * tolerance, kept);

        List<Point> simplified = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            if (kept[i]) {
                simplified.add(points.get(i));
            }
        }
        return List.copyOf(simplified);
    }

    private static void keepPoints(List<Point> points, int firstIndex, int lastIndex,
                                   double squaredTolerance, boolean[] kept) {
        if (lastIndex <= firstIndex + 1) {
            return;
        }

        Point firstPoint = points.get(firstIndex);
        Point lastPoint = points.get(lastIndex);
        double maxSquaredDistance = -1;
        int farthestIndex = -1;

        for (int i = firstIndex + 1; i < lastIndex; i++) {
            double squaredDistance = squaredDistanceToSegment(points.get(i), firstPoint, lastPoint);
            if (squaredDistance > maxSquaredDistance) {
                maxSquaredDistance = squaredDistance;
                farthestIndex = i;
            }
        }

        if (maxSquaredDistance > squaredTolerance) {
            kept[farthestIndex] = true;
            keepPoints(points, firstIndex, farthestIndex, squaredTolerance, kept);
            keepPoints(points, farthestIndex, lastIndex, squaredTolerance, kept);
        }
    }

    private static double squaredDistanceToSegment(Point point, Point segmentStart, Point segmentEnd) {
        double dx = segmentEnd.x() - segmentStart.x();
        double dy = segmentEnd.y() - segmentStart.y();
        double squaredLength = dx * dx + dy * dy;

        if (squaredLength == 0) {
            return squaredDistance(point, segmentStart);
        }

        double projectionRatio = ((point.x() - segmentStart.x()) * dx + (point.y() - segmentStart.y()) * dy)
                / squaredLength;
        double clampedRatio = Math.max(0, Math.min(1, projectionRatio));
        Point projectedPoint = new Point(
                segmentStart.x() + clampedRatio * dx,
                segmentStart.y() + clampedRatio * dy);

        return squaredDistance(point, projectedPoint);
    }

    private static double squaredDistance(Point p1, Point p2) {
        double dx = p1.x() - p2.x();
        double dy = p1.y() - p2.y();
        return dx * dx + dy * dy;
    }

    private record IndexPair(int firstIndex, int secondIndex) {
    }
}
