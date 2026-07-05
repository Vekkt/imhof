package ch.epfl.imhof.contours;

import ch.epfl.imhof.geometry.BezierPolyLine;
import ch.epfl.imhof.geometry.CubicBezierSegment;
import ch.epfl.imhof.geometry.Point;
import ch.epfl.imhof.geometry.PolyLine;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContourBezierSmootherTest {
    private static final double EPSILON = 1e-9;

    @Test
    void closedPathCreatesOneBezierSegmentPerAnchor() {
        List<Point> anchors = square();

        BezierPolyLine smoothed = assertInstanceOf(
                BezierPolyLine.class,
                ContourBezierSmoother.smoothClosed(anchors));

        assertEquals(anchors, smoothed.points());
        assertEquals(anchors.size(), smoothed.segments().size());
    }

    @Test
    void segmentEndpointsPreserveAnchors() {
        List<Point> anchors = square();

        BezierPolyLine smoothed = assertInstanceOf(
                BezierPolyLine.class,
                ContourBezierSmoother.smoothClosed(anchors));

        for (int i = 0; i < anchors.size(); i++) {
            CubicBezierSegment segment = smoothed.segments().get(i);
            assertEquals(anchors.get(i), segment.start());
            assertEquals(anchors.get((i + 1) % anchors.size()), segment.end());
        }
    }

    @Test
    void collinearAnchorsStayCollinear() {
        List<Point> anchors = List.of(
                new Point(0, 0),
                new Point(1, 0),
                new Point(2, 0),
                new Point(3, 0));

        BezierPolyLine smoothed = assertInstanceOf(
                BezierPolyLine.class,
                ContourBezierSmoother.smoothClosed(anchors));

        for (CubicBezierSegment segment : smoothed.segments()) {
            assertEquals(0, segment.control1().y(), EPSILON);
            assertEquals(0, segment.control2().y(), EPSILON);
            assertEquals(0, segment.start().y(), EPSILON);
            assertEquals(0, segment.end().y(), EPSILON);
        }
    }

    @Test
    void duplicatePointsDoNotProduceInvalidBezierCoordinates() {
        List<Point> anchors = List.of(
                new Point(0, 0),
                new Point(1, 0),
                new Point(1, 0),
                new Point(1, 1),
                new Point(0, 1),
                new Point(0, 0));

        BezierPolyLine smoothed = assertInstanceOf(
                BezierPolyLine.class,
                ContourBezierSmoother.smoothClosed(anchors));

        assertEquals(4, smoothed.points().size());
        for (CubicBezierSegment segment : smoothed.segments()) {
            assertFinite(segment.start());
            assertFinite(segment.control1());
            assertFinite(segment.control2());
            assertFinite(segment.end());
        }
    }

    @Test
    void sharpTurnsClampHandleLength() {
        List<Point> anchors = List.of(
                new Point(-100, 0),
                new Point(0, 0),
                new Point(0.1, 0),
                new Point(0.1, 100));

        BezierPolyLine smoothed = assertInstanceOf(
                BezierPolyLine.class,
                ContourBezierSmoother.smoothClosed(anchors));
        CubicBezierSegment shortSegment = smoothed.segments().get(1);

        double maxHandleLength = 0.1 * 0.45;
        assertTrue(distance(shortSegment.start(), shortSegment.control1()) <= maxHandleLength + EPSILON);
        assertTrue(distance(shortSegment.end(), shortSegment.control2()) <= maxHandleLength + EPSILON);
    }

    @Test
    void fewerThanFourPointsFallBackToStraightClosedPolyline() {
        PolyLine smoothed = ContourBezierSmoother.smoothClosed(List.of(
                new Point(0, 0),
                new Point(1, 0),
                new Point(0, 1)));

        assertFalse(smoothed instanceof BezierPolyLine);
        assertTrue(smoothed.isClosed());
    }

    private static List<Point> square() {
        return List.of(
                new Point(0, 0),
                new Point(1, 0),
                new Point(1, 1),
                new Point(0, 1));
    }

    private static void assertFinite(Point point) {
        assertTrue(Double.isFinite(point.x()));
        assertTrue(Double.isFinite(point.y()));
    }

    private static double distance(Point a, Point b) {
        return Math.hypot(a.x() - b.x(), a.y() - b.y());
    }
}
