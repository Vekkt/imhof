package ch.epfl.imhof.paintaing;

import ch.epfl.imhof.Preconditions;

public final class Color {
    public static final Color RED = new Color(1, 0, 0);
    public static final Color GREEN = new Color(0, 1, 0);
    public static final Color BLUE = new Color(0, 0, 1);
    public static final Color WHITE = new Color(1, 1, 1);
    public static final Color BLACK = new Color(0, 0, 0);

    private final float red;
    private final float green;
    private final float blue;

    private Color(float r, float g, float b) {
        this.red = r;
        this.green = g;
        this.blue = b;
    }

    public static Color gray(float level) {
        Preconditions.checkArgument(0 <= level && level <= 1);

        return new Color(level, level, level);
    }

    public static Color rgb(float r, float g, float b) {
        Preconditions.checkArgument(0 <= r && r <= 1);
        Preconditions.checkArgument(0 <= g && g <= 1);
        Preconditions.checkArgument(0 <= b && b <= 1);

        return new Color(r, g, b);
    }

    public static Color rgb(int values) {
        float b = (values & 0xFF) / 255f;
        float g = ((values >> 8) & 0xFF) / 255f;
        float r = ((values >> 16) & 0xFF) / 255f;

        Preconditions.checkArgument(0 <= r && r <= 1);
        Preconditions.checkArgument(0 <= g && g <= 1);
        Preconditions.checkArgument(0 <= b && b <= 1);

        return new Color(r, g, b);
    }

    public float red() {
        return this.red;
    }

    public float green() {
        return this.green;
    }

    public float blue() {
        return this.blue;
    }

    public Color mult(Color c1, Color c2) {
        return new Color(c1.red() * c2.red(),
                c1.green() * c2.green(),
                c1.blue() * c2.blue());
    }

    public static java.awt.Color convert(Color c) {
        return new java.awt.Color(c.red(), c.green(), c.blue());
    }

}
