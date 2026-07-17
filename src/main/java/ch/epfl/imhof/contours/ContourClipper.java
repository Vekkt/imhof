package ch.epfl.imhof.contours;

import ch.epfl.imhof.geometry.Point;
import ch.epfl.imhof.geometry.PolyLine;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public final class ContourClipper {
    private static final double EPSILON = 1e-7;

    private ContourClipper() {
    }

    public static List<PolyLine> clipToRectangle(PolyLine polyLine, Point corner1, Point corner2) {
        requireNonNull(polyLine);
        requireNonNull(corner1);
        requireNonNull(corner2);

        Rectangle rectangle = new Rectangle(
                Math.min(corner1.x(), corner2.x()),
                Math.max(corner1.x(), corner2.x()),
                Math.min(corner1.y(), corner2.y()),
                Math.max(corner1.y(), corner2.y()));

        List<Point> points = polyLine.points();
        if (points.size() < 2) {
            return List.of();
        }
        if (polyLine.isClosed() && points.stream().allMatch(rectangle::strictlyContains)) {
            return List.of(polyLine);
        }

        List<List<Point>> clippedPaths = new ArrayList<>();
        List<Point> currentPath = new ArrayList<>();
        int segmentCount = polyLine.isClosed() ? points.size() : points.size() - 1;

        for (int i = 0; i < segmentCount; i++) {
            Point start = points.get(i);
            Point end = points.get((i + 1) % points.size());
            boolean startInside = rectangle.strictlyContains(start);
            boolean endInside = rectangle.strictlyContains(end);

            if (startInside && endInside) {
                if (currentPath.isEmpty()) {
                    currentPath.add(start);
                }
                addIfNew(currentPath, end);
            } else if (startInside) {
                if (currentPath.isEmpty()) {
                    currentPath.add(start);
                }
                Segment clippedSegment = rectangle.clip(start, end);
                if (clippedSegment != null && !rectangle.isBoundarySegment(clippedSegment)) {
                    addIfNew(currentPath, clippedSegment.end());
                }
                finishPath(clippedPaths, currentPath);
                currentPath = new ArrayList<>();
            } else if (endInside) {
                finishPath(clippedPaths, currentPath);
                currentPath = new ArrayList<>();
                Segment clippedSegment = rectangle.clip(start, end);
                if (clippedSegment != null && !rectangle.isBoundarySegment(clippedSegment)) {
                    currentPath.add(clippedSegment.start());
                }
                addIfNew(currentPath, end);
            } else {
                finishPath(clippedPaths, currentPath);
                currentPath = new ArrayList<>();
            }
        }
        finishPath(clippedPaths, currentPath);

        if (polyLine.isClosed() && clippedPaths.size() > 1) {
            mergeConnectedEnds(clippedPaths);
        }

        return clippedPaths.stream()
                .filter(path -> path.size() >= 2)
                .map(ContourClipper::buildOpenPolyLine)
                .map(PolyLine.class::cast)
                .toList();
    }

    private static void mergeConnectedEnds(List<List<Point>> clippedPaths) {
        List<Point> firstPath = clippedPaths.getFirst();
        List<Point> lastPath = clippedPaths.getLast();

        if (!samePoint(lastPath.getLast(), firstPath.getFirst())) {
            return;
        }

        List<Point> mergedPath = new ArrayList<>(lastPath);
        firstPath.stream().skip(1).forEach(point -> addIfNew(mergedPath, point));
        clippedPaths.set(0, mergedPath);
        clippedPaths.removeLast();
    }

    private static void finishPath(List<List<Point>> clippedPaths, List<Point> path) {
        if (path.size() >= 2) {
            clippedPaths.add(path);
        }
    }

    private static void addIfNew(List<Point> points, Point point) {
        if (points.isEmpty() || !samePoint(points.getLast(), point)) {
            points.add(point);
        }
    }

    private static PolyLine buildOpenPolyLine(List<Point> points) {
        PolyLine.Builder builder = new PolyLine.Builder();
        points.forEach(builder::addPoint);
        return builder.buildOpen();
    }

    private static boolean samePoint(Point p1, Point p2) {
        return Math.abs(p1.x() - p2.x()) <= EPSILON && Math.abs(p1.y() - p2.y()) <= EPSILON;
    }

    private record Segment(Point start, Point end) {
    }

    private record Rectangle(double minX, double maxX, double minY, double maxY) {
        private boolean contains(Point point) {
            return minX - EPSILON <= point.x()
                    && point.x() <= maxX + EPSILON
                    && minY - EPSILON <= point.y()
                    && point.y() <= maxY + EPSILON;
        }

        private boolean strictlyContains(Point point) {
            return minX + EPSILON < point.x()
                    && point.x() < maxX - EPSILON
                    && minY + EPSILON < point.y()
                    && point.y() < maxY - EPSILON;
        }

        private boolean isBoundarySegment(Segment segment) {
            return sameCoordinate(segment.start().x(), minX) && sameCoordinate(segment.end().x(), minX)
                    || sameCoordinate(segment.start().x(), maxX) && sameCoordinate(segment.end().x(), maxX)
                    || sameCoordinate(segment.start().y(), minY) && sameCoordinate(segment.end().y(), minY)
                    || sameCoordinate(segment.start().y(), maxY) && sameCoordinate(segment.end().y(), maxY);
        }

        private boolean sameCoordinate(double c1, double c2) {
            return Math.abs(c1 - c2) <= EPSILON;
        }

        private Segment clip(Point start, Point end) {
            double dx = end.x() - start.x();
            double dy = end.y() - start.y();
            double[] range = {0d, 1d};

            if (!clipEdge(-dx, start.x() - minX, range)
                    || !clipEdge(dx, maxX - start.x(), range)
                    || !clipEdge(-dy, start.y() - minY, range)
                    || !clipEdge(dy, maxY - start.y(), range)) {
                return null;
            }

            return new Segment(interpolate(start, dx, dy, range[0]), interpolate(start, dx, dy, range[1]));
        }

        private boolean clipEdge(double p, double q, double[] range) {
            if (Math.abs(p) <= EPSILON) {
                return q >= -EPSILON;
            }

            double r = q / p;
            if (p < 0) {
                if (r > range[1]) {
                    return false;
                }
                range[0] = Math.max(range[0], r);
            } else {
                if (r < range[0]) {
                    return false;
                }
                range[1] = Math.min(range[1], r);
            }

            return true;
        }

        private Point interpolate(Point start, double dx, double dy, double t) {
            return new Point(start.x() + t * dx, start.y() + t * dy);
        }
    }
}
