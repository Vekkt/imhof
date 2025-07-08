package ch.epfl.imhof;

import ch.epfl.imhof.geometry.Point;
import ch.epfl.imhof.geometry.PolyLine;
import ch.epfl.imhof.projection.Projection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Grid {
    private final static int GRID_STEP_METERS = 1000;
    private final ArrayList<Attributed<PolyLine>> gridLines = new ArrayList<>();

    public Grid(Projection projection, PointGeo bottomLeft, PointGeo topRight) {
        Point projectedBottomLeft = projection.project(bottomLeft);
        Point projectedTopRight = projection.project(topRight);

        int gridHStart = Math.ceilDiv((int) Math.ceil(projectedBottomLeft.x()), GRID_STEP_METERS) * GRID_STEP_METERS;
        int gridHEnd = Math.floorDiv((int) Math.floor(projectedTopRight.x()), GRID_STEP_METERS) * GRID_STEP_METERS;

        int gridVStart = Math.ceilDiv((int) Math.ceil(projectedBottomLeft.y()), GRID_STEP_METERS) * GRID_STEP_METERS;
        int gridVEnd = Math.floorDiv((int) Math.floor(projectedTopRight.y()), GRID_STEP_METERS) * GRID_STEP_METERS;

        List<Point> linePoints;
        for (int x = gridHStart; x <= gridHEnd; x += GRID_STEP_METERS) {
            linePoints = new ArrayList<>();
            linePoints.add(new Point(x, projectedBottomLeft.y()));
            for (int y = gridVStart; y <= gridVEnd; y += GRID_STEP_METERS) {
                linePoints.add(new Point(x, y));
            }
            linePoints.add(new Point(x, projectedTopRight.y()));
            gridLines.add(buildLine(linePoints));
        }

        for (int y = gridVStart; y <= gridVEnd; y += GRID_STEP_METERS) {
            linePoints = new ArrayList<>();
            linePoints.add(new Point(projectedBottomLeft.x(), y));
            for (int x = gridHStart; x <= gridHEnd; x += GRID_STEP_METERS) {
                linePoints.add(new Point(x, y));
            }
            linePoints.add(new Point(projectedTopRight.x(), y));
            gridLines.add(buildLine(linePoints));
        }
    }

    private static Attributed<PolyLine> buildLine(List<Point> linePoints) {
        PolyLine.Builder gridLineBuilder = new PolyLine.Builder();
        Attributes gridAttributes = new Attributes(Collections.singletonMap("grid_line", "yes"));

        for (Point n: linePoints)
            gridLineBuilder.addPoint(n);

        return new Attributed<>(gridLineBuilder.buildOpen(), gridAttributes);
    }

    public ArrayList<Attributed<PolyLine>> getLines() {
        return gridLines;
    }
}
