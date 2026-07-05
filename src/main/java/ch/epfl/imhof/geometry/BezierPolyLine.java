package ch.epfl.imhof.geometry;

import ch.epfl.imhof.Preconditions;

import java.util.List;

import static java.util.Objects.requireNonNull;

public final class BezierPolyLine extends PolyLine {
    private final List<CubicBezierSegment> segments;

    public BezierPolyLine(List<Point> points, List<CubicBezierSegment> segments) {
        super(points);
        requireNonNull(segments);
        Preconditions.checkArgument(!segments.isEmpty());
        Preconditions.checkArgument(segments.size() == points().size());

        this.segments = List.copyOf(segments);
    }

    @Override
    public boolean isClosed() {
        return true;
    }

    public List<CubicBezierSegment> segments() {
        return List.copyOf(segments);
    }
}
