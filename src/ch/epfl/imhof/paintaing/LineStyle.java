package ch.epfl.imhof.paintaing;

import ch.epfl.imhof.Preconditions;

import java.util.Arrays;

public final class LineStyle {
    public enum LineCap {
        Butt,
        Round,
        Square
    }

    public enum LineJoin {
        Bevel,
        Miter,
        Round
    }

    private Color color;
    private LineCap cap;
    private LineJoin join;
    private float stroke;
    private float[] pattern;

    public LineStyle(float stroke, Color color, LineCap cap, LineJoin join, float... pattern) {
        Preconditions.checkArgument(stroke >= 0);
        for (float f: pattern)
            Preconditions.checkArgument(f > 0);

        this.color = color;
        this.cap = cap;
        this.join = join;
        this.stroke = stroke;
        this.pattern = Arrays.copyOf(pattern, pattern.length);
    }

    public LineStyle(Color color, float stroke) {
        this(stroke, color, LineCap.Butt, LineJoin.Miter);
    }

    public Color getColor() {
        return color;
    }

    public LineCap getCap() {
        return cap;
    }

    public LineJoin getJoin() {
        return join;
    }

    public float getStroke() {
        return stroke;
    }

    public float[] getPattern() {
        return (pattern.length > 0) ? Arrays.copyOf(pattern, pattern.length) : null;
    }

    public LineStyle withColor(Color color) {
        return new LineStyle(
                this.stroke,
                color,
                this.cap,
                this.join,
                this.pattern);
    }

    public LineStyle withCap(LineCap cap) {
        return new LineStyle(
                this.stroke,
                this.color,
                cap,
                this.join,
                this.pattern);
    }

    public LineStyle withJoin(LineJoin join) {
        return new LineStyle(
                this.stroke,
                this.color,
                this.cap,
                join,
                this.pattern);
    }

    public LineStyle withStroke(float stroke) {
        return new LineStyle(
                stroke,
                this.color,
                this.cap,
                this.join,
                this.pattern);
    }

    public LineStyle withPattern(float[] pattern) {
        return new LineStyle(
                this.stroke,
                this.color,
                this.cap,
                this.join,
                pattern);
    }
}
