package ch.epfl.imhof;

public interface Preconditions {
    static void checkArgument(boolean b) {
        if (!b) {
            throw new IllegalArgumentException();
        }
    }

    static void checkArgument(boolean b, String m) {
        if (!b) {
            throw new IllegalArgumentException(m);
        }
    }

    static void inCloseBounds(double left, double val, double right) {
        if (!(left <= val && val <= right)) {
            throw new IllegalArgumentException();
        }
    }
}
