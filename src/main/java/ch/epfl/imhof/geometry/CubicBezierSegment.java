package ch.epfl.imhof.geometry;

import static java.util.Objects.requireNonNull;

public record CubicBezierSegment(Point start, Point control1, Point control2, Point end) {
    public CubicBezierSegment {
        requireNonNull(start);
        requireNonNull(control1);
        requireNonNull(control2);
        requireNonNull(end);
    }
}
