package ch.epfl.imhof;

import ch.epfl.imhof.geometry.Point;
import ch.epfl.imhof.geometry.PolyLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Grid {
    private final static int GRID_STEP_METERS = 1000;
    private final ArrayList<Attributed<PolyLine>> gridLines = new ArrayList<>();

    Grid(Point projectedBottomLeft, Point projectedTopRight) {
        int gridHStart = Math.ceilDiv((int) Math.ceil(projectedBottomLeft.x()), GRID_STEP_METERS) * GRID_STEP_METERS;
        int gridHEnd = Math.floorDiv((int) Math.floor(projectedTopRight.x()), GRID_STEP_METERS) * GRID_STEP_METERS;

        int gridVStart = Math.ceilDiv((int) Math.ceil(projectedBottomLeft.y()), GRID_STEP_METERS) * GRID_STEP_METERS;
        int gridVEnd = Math.floorDiv((int) Math.floor(projectedTopRight.y()), GRID_STEP_METERS) * GRID_STEP_METERS;

        buildGrid(projectedBottomLeft, projectedTopRight, gridHStart, gridHEnd, gridVStart, gridVEnd);
    }

    private void buildGrid(Point bl, Point tr, int gridHStart, int gridHEnd, int gridVStart, int gridVEnd) {
        List<Point> linePoints;
        for (int x = gridHStart; x <= gridHEnd; x += GRID_STEP_METERS) {
            linePoints = new ArrayList<>();
            linePoints.add(new Point(x, bl.y()));
            for (int y = gridVStart; y <= gridVEnd; y += GRID_STEP_METERS) {
                linePoints.add(new Point(x, y));
            }
            linePoints.add(new Point(x, tr.y()));
            gridLines.add(buildGridLine(linePoints));
        }

        for (int y = gridVStart; y <= gridVEnd; y += GRID_STEP_METERS) {
            linePoints = new ArrayList<>();
            linePoints.add(new Point(bl.x(), y));
            for (int x = gridHStart; x <= gridHEnd; x += GRID_STEP_METERS) {
                linePoints.add(new Point(x, y));
            }
            linePoints.add(new Point(tr.x(), y));
            gridLines.add(buildGridLine(linePoints));
        }
    }

    private static Attributed<PolyLine> buildGridLine(List<Point> linePoints) {
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
