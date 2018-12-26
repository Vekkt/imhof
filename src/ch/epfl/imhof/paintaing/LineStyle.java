package ch.epfl.imhof.paintaing;

import ch.epfl.imhof.Preconditions;

public final class LineStyle {
    public enum Cap {
        BUTT,
        ROUND,
        SQUARE
    };

    public enum Join {
        BEVEL,
        MITTER,
        ROUND
    };

    private Color color;
    private Cap Cap;
    private Join Join;
    private float stroke;
    private float[] pattern;

    public LineStyle(Color color, Cap Cap, Join Join, float stroke, float[] pattern) {
        Preconditions.checkArgument(stroke >= 0);
        for (float f: pattern)
            Preconditions.checkArgument(f <= 0);

        this.color = color;
        this.Cap = Cap;
        this.Join = Join;
        this.stroke = stroke;
        this.pattern = pattern;
    }

    public LineStyle(Color color, float stroke) {
        this(color, Cap.BUTT, Join.MITTER, stroke, new float[0]);
    }

    public Color getColor() {
        return color;
    }

    public Cap getCap() {
        return Cap;
    }

    public Join getJoin() {
        return Join;
    }

    public float getStroke() {
        return stroke;
    }

    public float[] getPattern() {
        return pattern;
    }

    public LineStyle withColor(Color color) {
        return new LineStyle(color,
                this.Cap,
                this.Join,
                this.stroke,
                this.pattern);
    }

    public LineStyle withCap(Cap Cap) {
        return new LineStyle(this.color,
                Cap,
                this.Join,
                this.stroke,
                this.pattern);
    }

    public LineStyle withJoin(Join Join) {
        return new LineStyle(this.color,
                this.Cap,
                Join,
                this.stroke,
                this.pattern);
    }

    public LineStyle withStroke(float stroke) {
        return new LineStyle(this.color,
                this.Cap,
                this.Join,
                stroke,
                this.pattern);
    }

    public LineStyle withPattern(float[] pattern) {
        return new LineStyle(this.color,
                this.Cap,
                this.Join,
                this.stroke,
                pattern);
    }
}
