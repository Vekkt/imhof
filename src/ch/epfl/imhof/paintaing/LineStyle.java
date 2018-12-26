package ch.epfl.imhof.paintaing;

import ch.epfl.imhof.Preconditions;

public final class LineStyle {
    public enum Termination {
        BUTT,
        ROUND,
        SQUARE
    };

    public enum Joining {
        BEVEL,
        MITTER,
        ROUND
    };

    private Color color;
    private Termination termination;
    private Joining joining;
    private float stroke;
    private float[] pattern;

    public LineStyle(Color color, Termination termination, Joining joining, float stroke, float[] pattern) {
        Preconditions.checkArgument(stroke >= 0);
        for (float f: pattern)
            Preconditions.checkArgument(f <= 0);

        this.color = color;
        this.termination = termination;
        this.joining = joining;
        this.stroke = stroke;
        this.pattern = pattern;
    }

    public LineStyle(Color color, float stroke) {
        this(color, Termination.BUTT, Joining.MITTER, stroke, new float[0]);
    }

    public Color getColor() {
        return color;
    }

    public Termination getTermination() {
        return termination;
    }

    public Joining getJoining() {
        return joining;
    }

    public float getStroke() {
        return stroke;
    }

    public float[] getPattern() {
        return pattern;
    }

    public LineStyle withColor(Color color) {
        return new LineStyle(color,
                this.termination,
                this.joining,
                this.stroke,
                this.pattern);
    }

    public LineStyle withTermination(Termination termination) {
        return new LineStyle(this.color,
                termination,
                this.joining,
                this.stroke,
                this.pattern);
    }

    public LineStyle withjoining(Joining joining) {
        return new LineStyle(this.color,
                this.termination,
                joining,
                this.stroke,
                this.pattern);
    }

    public LineStyle withStroke(float stroke) {
        return new LineStyle(this.color,
                this.termination,
                this.joining,
                stroke,
                this.pattern);
    }

    public LineStyle withPattern(float[] pattern) {
        return new LineStyle(this.color,
                this.termination,
                this.joining,
                this.stroke,
                pattern);
    }
}
