package ch.epfl.imhof.paintaing;

import ch.epfl.imhof.geometry.ClosedPolyLine;
import ch.epfl.imhof.geometry.Point;
import ch.epfl.imhof.geometry.PolyLine;
import ch.epfl.imhof.geometry.Polygon;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Function;

import static ch.epfl.imhof.paintaing.Color.convert;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;

public final class Java2DCanvas implements Canvas {
    private final Function<Point, Point> coordChange;
    private final BufferedImage image;
    private final Graphics2D ctx;

    public Java2DCanvas(Point bl, Point tr, int width, int height, int dpi, Color bg) {
        double dilatation = dpi / 72d;

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ctx = image.createGraphics();
        ctx.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        ctx.setColor(convert(bg));
        ctx.fillRect(0, 0, width, height);
        ctx.translate(width / 2d, height / 2d);
        ctx.scale(dilatation, dilatation);
        coordChange = Point.alignedCoordinateChange(
                bl, new Point((-width / 2d) / dilatation, (height / 2d) / dilatation),
                tr, new Point((width / 2d) / dilatation, (-height / 2d) / dilatation));
    }

    public BufferedImage image() {
        return image;
    }

    public void drawPolyLine(PolyLine p, LineStyle s) {
        ctx.setColor(convert(s.getColor()));

        ctx.draw(getPath(p));
    }

    private Path2D getPath(PolyLine polyline) {
        Path2D path = new Path2D.Double();

        /* Un PolyLine a toujours au moins un point */
        Point firstPoint = this.coordChange.apply(polyline.firstPoint());
        path.moveTo(firstPoint.x(), firstPoint.y());

        polyline.points()
                .stream()
                .skip(1)
                .map(this.coordChange)
                .forEach( p -> path.lineTo(p.x(), p.y()) );

        if (polyline.isClosed())
            path.closePath();

        return path;
    }

    public void drawPolygon(Polygon p, Color c) {
        ctx.setColor(convert(c));

        Area polygon = new Area(getPath(p.shell()));
        for(ClosedPolyLine hole : p.holes())
            polygon.subtract(new Area(getPath(hole)));

        ctx.fill(polygon);
    }

    public static void findDifferences(BufferedImage image, BufferedImage reference) throws IOException {
        int diffs = 0;
        for (int i = 0; i < reference.getWidth(); i++) {
            for (int j = 0; j < reference.getHeight(); j++) {
                if (image.getRGB(i, j) != reference.getRGB(i, j)) {
                    reference.setRGB(i, j, 0xFF_00_00);
                    diffs++;
                }
            }
        }

        ImageIO.write(reference, "png", new File("diffs.png"));
        System.out.println(diffs + " different pixels found.");
    }
}
