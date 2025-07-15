package ch.epfl.imhof.contours;

import ch.epfl.imhof.geometry.Point;


public class IsoCell {
    public enum Side {
        LEFT, RIGHT, TOP, BOTTOM, NONE
    };
    boolean flipped;
    int cellBits;
    double left, right, top, bottom;


    public IsoCell(double level, double[] elevations, boolean interp) {
        this.cellBits = getCellBits(level, elevations);
        this.flipped = getFlipped(level, elevations);

        if (interp) this.interpolateCoords(elevations, level);
        else setInitialCoords();
    }

    private static int getCellBits(double level, double[] elevations) {
        int ll = elevations[2] > level ? 1 : 0;
        int lr = elevations[3] > level ? 2 : 0;
        int tr = elevations[1] > level ? 4 : 0;
        int tl = elevations[0] > level ? 8 : 0;

        return ll | lr | tr | tl;
    }

    private boolean getFlipped(double level, double[] elevations) {
        boolean isFlipped = false;
        if (cellBits == 0b0101 || cellBits == 0b1010) {
            double center = (elevations[0] + elevations[1] + elevations[2] + elevations[3]) / 4;
            isFlipped = (center < level);
        }
        return isFlipped;
    }

    public int getCellBits() {
        return cellBits;
    }

    public Point fromPoint(Side side, Point p) {
        return switch (side) {
            case BOTTOM -> new Point(p.x() + bottom, p.y() + 1);
            case RIGHT -> new Point(p.x() + 1, p.y() + right);
            case TOP -> new Point(p.x() + top, p.y());
            case LEFT -> new Point(p.x(), p.y() + left);
            default -> throw new IllegalArgumentException();
        };
    }

    public Side firstSide(Side prev) {
        return switch (cellBits) {
            case 1, 3, 7 -> Side.LEFT;
            case 2, 6, 14 -> Side.BOTTOM;
            case 4, 12, 13 -> Side.RIGHT;
            case 8, 9, 11 -> Side.TOP;
            case 5 -> switch (prev) {
                case LEFT -> Side.RIGHT;
                case RIGHT -> Side.LEFT;
                default -> throw new IllegalArgumentException();
            };
            case 10 -> switch (prev) {
                case BOTTOM -> Side.TOP;
                case TOP -> Side.BOTTOM;
                default -> throw new IllegalArgumentException();
            };
            default -> throw new IllegalArgumentException();
        };
    }

    public Side secondSide(Side prev) {
        return switch (cellBits) {
            case 8, 12, 14 -> Side.LEFT;
            case 1, 9, 13 -> Side.BOTTOM;
            case 2, 3, 11 -> Side.RIGHT;
            case 4, 6, 7 -> Side.TOP;
            case 5 -> switch (prev) {
                case LEFT -> flipped ? Side.BOTTOM : Side.TOP;
                case RIGHT -> flipped ? Side.TOP : Side.BOTTOM;
                default -> throw new IllegalArgumentException();
            };
            case 10 -> switch (prev) {
                case BOTTOM -> flipped ? Side.RIGHT : Side.LEFT;
                case TOP -> flipped ? Side.LEFT : Side.RIGHT;
                default -> throw new IllegalArgumentException();
            };
            default -> throw new IllegalArgumentException();
        };
    }

    public void clearIso() {
        switch (cellBits) {
            case 0, 5, 10, 15:
                break;
            default:
                cellBits = 15;
        }
    }

    private void setInitialCoords() {
        switch (cellBits) {case 1, 3, 5, 7, 8, 10, 12, 14 -> left = 0.5;}
        switch (cellBits) {case 1, 2, 5, 6, 9, 10, 13, 14 -> bottom = 0.5;}
        switch (cellBits) {case 4, 5, 6, 7, 8, 9, 10, 11 -> top = 0.5;}
        switch (cellBits) {case 2, 3, 4, 5, 10, 11, 12, 13 -> right = 0.5;}
    }
    
    public void interpolateCoords(double[] cellElevation, double threshold) {
        double tl = cellElevation[0];
        double tr = cellElevation[1];
        double ll = cellElevation[2];
        double lr = cellElevation[3];

        switch (cellBits) {case 1, 3, 5, 7, 8, 10, 12, 14 -> left = (tl - threshold) / (tl - ll);}
        switch (cellBits) {case 1, 2, 5, 6, 9, 10, 13, 14 -> bottom = (threshold - ll) / (lr - ll);}
        switch (cellBits) {case 4, 5, 6, 7, 8, 9, 10, 11 -> top = (threshold - tl) / (tr - tl);}
        switch (cellBits) {case 2, 3, 4, 5, 10, 11, 12, 13 -> right = (tr - threshold) / (tr - lr);}
    }
}
