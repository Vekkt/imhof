package ch.epfl.imhof.dem;

import ch.epfl.imhof.PointGeo;
import ch.epfl.imhof.geometry.Point;
import ch.epfl.imhof.projection.Projection;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public final class ElevationView {
    private final Projection projection;
    private final DigitalElevationModel dem;
    Function<Point, Point> coordinateChange;
    private final int width;
    private final int height;

    private final short minElevation;
    private final short maxElevation;

    public ElevationView(Projection proj, DigitalElevationModel dem, int width, int height, Function<Point, Point> ref) {
        this.projection = requireNonNull(proj);
        this.dem = requireNonNull(dem);
        this.coordinateChange = requireNonNull(ref);
        this.width = width;
        this.height = height;

        short maxElevation_ = Short.MIN_VALUE;
        short minElevation_ = Short.MAX_VALUE;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                PointGeo p = projection.inverse(coordinateChange.apply(new Point(i, j)));
                short currentElevation = dem.bufferAt(p);
                maxElevation_ = currentElevation > maxElevation_ ? currentElevation: maxElevation_;
                minElevation_ = currentElevation < minElevation_ ? currentElevation: minElevation_;
            }
        }

        minElevation = minElevation_;
        maxElevation = maxElevation_;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public short minElevation() { return minElevation; }

    public short maxElevation() { return maxElevation; }

    private double safeBufferAt(int i, int j) {
        if (i > width || j > height || i < 0 || j < 0) {
            return Double.MIN_VALUE;
        }
        PointGeo p = projection.inverse(coordinateChange.apply(new Point(i, j)));
        return dem.bufferAt(p);
    }

    public double[] getPaddedElevation(int i, int j) {
        return new double[]{
                safeBufferAt(i, j),
                safeBufferAt(i + 1, j),
                safeBufferAt(i, j + 1),
                safeBufferAt(i + 1, j + 1),
        };
    }
}
