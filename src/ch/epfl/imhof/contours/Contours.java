package ch.epfl.imhof.contours;

import ch.epfl.imhof.Attributed;
import ch.epfl.imhof.Attributes;
import ch.epfl.imhof.PointGeo;
import ch.epfl.imhof.dem.DigitalElevationModel;
import ch.epfl.imhof.dem.HGTDigitalElevationModel;
import ch.epfl.imhof.geometry.PolyLine;
import ch.epfl.imhof.geometry.Point;
import ch.epfl.imhof.projection.Projection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static ch.epfl.imhof.geometry.Point.alignedCoordinateChange;
import static java.util.Objects.requireNonNull;

public class Contours {
    private final static double CONTOUR_STEP = 20;
    private final List<Attributed<PolyLine>> contourLines = new ArrayList<>();
    private final List<Double> levels = new ArrayList<>();;
    private final double[][] elevations;

    public Contours(Projection proj, DigitalElevationModel dem, Point projectedBottomLeft, Point projectedTopRight,
                    int width, int height) {
        requireNonNull(proj);
        requireNonNull(dem);

        Function<Point, Point> coordChange = Point.alignedCoordinateChange(
                new Point(0, height), projectedBottomLeft,
                new Point(width, 0), projectedTopRight
        );

        double[][] data = new double[height][width];
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                PointGeo p = proj.inverse(coordChange.apply(new Point(i, j)));
                data[j][i] = ((HGTDigitalElevationModel) dem).bufferAt(p);
            }
        }

        this.elevations = prepareData(data);

        // Shift coordinates by (-1,-1) for padding
        Function<Point, Point> ref = alignedCoordinateChange(
                new Point(1, this.elevations.length + 1), projectedBottomLeft,
                new Point(this.elevations[0].length + 1, 1), projectedTopRight
        );

        IsoCell[][] levelContours;
        for (double level: levels) {
            levelContours = constructIsoMap(level);
            contourLines.addAll(buildLevelPolyLines(levelContours, ref, level));
        }
    }

    private double[][] prepareData(double[][] data) {
        int rows = data.length;
        int cols = data[0].length;
        double[][] padded = new double[rows + 2][cols + 2];

        double minLevel = Float.MAX_VALUE;
        double maxLevel = Float.MIN_VALUE;
        double currentElevation;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                currentElevation = data[i][j];
                padded[i + 1][j + 1] = currentElevation;
                minLevel = Math.min(currentElevation, minLevel);
                maxLevel = Math.max(currentElevation, maxLevel);
            }
        }
        minLevel--;

        for (int i = 0; i < cols + 2; i++) {
            padded[0][i] = minLevel;
            padded[rows + 1][i] = minLevel;
        }
        for (int i = 0; i < rows + 2; i++) {
            padded[i][0] = minLevel;
            padded[i][cols + 1] = minLevel;
        }

        for (double level = CONTOUR_STEP * Math.ceilDiv((int) minLevel+1, (int) CONTOUR_STEP);
             level < maxLevel; level += CONTOUR_STEP)
            this.levels.add(level);

        return padded;
    }

    private IsoCell[][] constructIsoMap(double level) {
        double[] elevations;
        int width = this.elevations[0].length;
        int height = this.elevations.length;
        IsoCell[][] contours = new IsoCell[height - 1][width - 1];
        for (int j = 0; j < height - 1; j++) {
            for (int i = 0; i < width - 1; i++) {
                elevations = getPaddedElevation(i, j);
                contours[j][i] = new IsoCell(level, elevations, true);
            }
        }
        return contours;
    }

    private List<Attributed<PolyLine>> buildLevelPolyLines(IsoCell[][] levelContours, Function<Point, Point> ref, double level) {
        List<Attributed<PolyLine>> levelPolyLines = new ArrayList<>();

        for (int j = 0; j < levelContours.length - 1; j++) {
            for (int i = 0; i < levelContours[0].length - 1; i++) {
                if (levelContours[j][i].getCellBits() != 0
                        && levelContours[j][i].getCellBits() != 5
                        && levelContours[j][i].getCellBits() != 10
                        && levelContours[j][i].getCellBits() != 15) {
                    levelPolyLines.add(buildLevelSubContour(levelContours, i, j, ref, level));
                }
            }
        }

        return levelPolyLines;
    }
    
    private Attributed<PolyLine> buildLevelSubContour(IsoCell[][] levelContours, int i, int j, Function<Point, Point> ref, double level) {
        Attributes contourAttributes = new Attributes(
                Collections.singletonMap((((int) level) % 100 == 0) ? "major_contour": "minor_contour", "")
        );
        PolyLine.Builder polyLineBuilder = new PolyLine.Builder();

        IsoCell.Side prevSide = IsoCell.Side.NONE;
        IsoCell.Side nextSide;
        IsoCell start = levelContours[j][i];

        nextSide = start.firstSide(prevSide);
        polyLineBuilder.addPoint(ref.apply(start.fromPoint(nextSide, new Point(i, j))));
        nextSide = start.secondSide(nextSide);
        polyLineBuilder.addPoint(ref.apply(start.fromPoint(nextSide, new Point(i, j))));

        switch (nextSide) {
            case BOTTOM -> j += 1;
            case LEFT -> i -= 1;
            case RIGHT -> i += 1;
            case TOP -> j -= 1;
        }
        start.clearIso();

        IsoCell curCell;
        while ((curCell = levelContours[j][i]) != start) {
            nextSide = curCell.secondSide(nextSide);
            polyLineBuilder.addPoint(ref.apply(curCell.fromPoint(nextSide, new Point(i, j))));

            switch (nextSide) {
                case BOTTOM -> j += 1;
                case LEFT -> i -= 1;
                case RIGHT -> i += 1;
                case TOP -> j -= 1;
            }
            curCell.clearIso();
        }

        return new Attributed<>(polyLineBuilder.buildClosed(), contourAttributes);
    }

    private double[] getPaddedElevation(int i, int j) {
        return new double[]{
                this.elevations[j][i],
                this.elevations[j][i+1],
                this.elevations[j+1][i],
                this.elevations[j+1][i+1],
        };
    }

    public List<Attributed<PolyLine>> getContourLines() {
        return contourLines;
    }
}
