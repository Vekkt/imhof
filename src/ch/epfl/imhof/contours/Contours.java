package ch.epfl.imhof.contours;

import ch.epfl.imhof.Attributed;
import ch.epfl.imhof.Attributes;
import ch.epfl.imhof.dem.DigitalElevationModel;
import ch.epfl.imhof.dem.ElevationView;
import ch.epfl.imhof.geometry.PolyLine;
import ch.epfl.imhof.geometry.Point;
import ch.epfl.imhof.projection.Projection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static ch.epfl.imhof.geometry.Point.alignedCoordinateChange;
import static java.util.Objects.requireNonNull;


public final class Contours {
    private final static double CONTOUR_STEP = 20;
    private final static double MAJOR_CONTOUR_STEP = 100;
    private final List<Attributed<PolyLine>> contourLines = new ArrayList<>();
    private final List<Double> levels = new ArrayList<>();
    private final ElevationView elevations;

    public Contours(Projection proj, DigitalElevationModel dem, Point projectedBottomLeft, Point projectedTopRight,
                    int width, int height) {
        requireNonNull(proj);
        requireNonNull(dem);

        Function<Point, Point> coordChange = Point.alignedCoordinateChange(
                new Point(0, height), projectedBottomLeft,
                new Point(width, 0), projectedTopRight
        );

        this.elevations = new ElevationView(proj, dem, width, height, coordChange);

        double zeroElevation = Math.round((float) elevations.minElevation() / CONTOUR_STEP) * CONTOUR_STEP;
        for (double i = zeroElevation; i < elevations.maxElevation(); i += CONTOUR_STEP) {
            levels.add(i);
        }

        // Shift coordinates by (-1,-1) for padding
        Function<Point, Point> ref = alignedCoordinateChange(
                new Point(1, elevations.height() + 1), projectedBottomLeft,
                new Point(elevations.width() + 1, 1), projectedTopRight
        );

        IsoCell[][] levelContours;
        for (double level: levels) {
            levelContours = constructIsoMap(level);
            contourLines.addAll(buildLevelPolyLines(levelContours, ref, level));
        }
    }


    private IsoCell[][] constructIsoMap(double level) {
        double[] elevationsAt;
        int width = elevations.width();
        int height = elevations.height();
        IsoCell[][] contours = new IsoCell[height + 2][width + 2];
        for (int j = -1; j < height + 1; j++) {
            for (int i = -1; i < width + 1; i++) {
                elevationsAt = elevations.getPaddedElevation(i, j);
                contours[j + 1][i + 1] = new IsoCell(level, elevationsAt, true);
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
                Collections.singletonMap((((int) level) % MAJOR_CONTOUR_STEP == 0) ? "major_contour": "minor_contour", "")
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

    public List<Attributed<PolyLine>> getContourLines() {
        return contourLines;
    }
}
