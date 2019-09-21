package ch.epfl.imhof.paintaing;

import ch.epfl.imhof.Preconditions;

public final class Color {
    public static final Color RED = new Color(1, 0, 0);
    public static final Color GREEN = new Color(0, 1, 0);
    public static final Color BLUE = new Color(0, 0, 1);
    public static final Color WHITE = new Color(1, 1, 1);
    public static final Color BLACK = new Color(0, 0, 0);

    private final double red;
    private final double green;
    private final double blue;

    private Color(double r, double g, double b) {
        this.red = r;
        this.green = g;
        this.blue = b;
    }

    public static Color gray(double level) {
        Preconditions.checkArgument(0 <= level && level <= 1);

        return new Color(level, level, level);
    }

    public static Color rgb(double r, double g, double b) {
        Preconditions.checkArgument(0 <= r && r <= 1);
        Preconditions.checkArgument(0 <= g && g <= 1);
        Preconditions.checkArgument(0 <= b && b <= 1);

        return new Color(r, g, b);
    }

    public static Color rgb(int values) {
        double b = (values & 0xFF) / 255d;
        double g = ((values >> 8) & 0xFF) / 255d;
        double r = ((values >> 16) & 0xFF) / 255d;

        Preconditions.checkArgument(0 <= r && r <= 1);
        Preconditions.checkArgument(0 <= g && g <= 1);
        Preconditions.checkArgument(0 <= b && b <= 1);

        return new Color(r, g, b);
    }

    public double red() {
        return this.red;
    }

    public double green() {
        return this.green;
    }

    public double blue() {
        return this.blue;
    }

    public static Color mult(Color c1, Color c2) {
        return new Color(c1.red() * c2.red(),
                c1.green() * c2.green(),
                c1.blue() * c2.blue());
    }

    public static java.awt.Color convert(Color c) {
        return new java.awt.Color((float) c.red(), (float) c.green(), (float) c.blue());
    }

}
