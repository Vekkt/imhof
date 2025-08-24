package ch.epfl.imhof;

import ch.epfl.imhof.contours.Contours;
import ch.epfl.imhof.dem.DigitalElevationModel;
import ch.epfl.imhof.dem.Earth;
import ch.epfl.imhof.dem.HGTDigitalElevationModel;
import ch.epfl.imhof.dem.ReliefShader;
import ch.epfl.imhof.geometry.Point;
import ch.epfl.imhof.osm.OSMMap;
import ch.epfl.imhof.osm.OSMMapReader;
import ch.epfl.imhof.osm.OSMToGeoTransformer;
import ch.epfl.imhof.paintaing.Color;
import ch.epfl.imhof.paintaing.Border;
import ch.epfl.imhof.paintaing.Java2DCanvas;
import ch.epfl.imhof.paintaing.SwissPainter;
import ch.epfl.imhof.projection.CH1903Projection;
import ch.epfl.imhof.projection.EquirectangularProjection;
import ch.epfl.imhof.projection.Projection;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import static ch.epfl.imhof.paintaing.Color.convert;
import static ch.epfl.imhof.paintaing.Color.mult;

public final class Main {
    private final static Vector3 LIGHT_SOURCE = new Vector3(-1, 1, 1);


    public static void main(String[] args) throws Exception {
        PointGeo bottomLeft = new PointGeo(
                Math.toRadians(Double.parseDouble(args[3])),
                Math.toRadians(Double.parseDouble(args[2])));
        PointGeo topRight = new PointGeo(
                Math.toRadians(Double.parseDouble(args[5])),
                Math.toRadians(Double.parseDouble(args[4])));


        int dpi = Integer.parseInt(args[6]);
        int pixelPerMeterResolution = (int) Math.round(dpi * (5000d / 127d));


        int height = (int) Math.round(pixelPerMeterResolution
                * (topRight.latitude() - bottomLeft.latitude()) * Earth.RADIUS
                / 25000d);

        Projection projection;

        if (args.length > 8) {
            projection = switch (args[8]) {
                case "CH1903" -> new CH1903Projection();
                case "Equirectangular" -> new EquirectangularProjection();
                default -> throw new IllegalArgumentException("Nom de projection invalide.");
            };
        } else {
            projection = new CH1903Projection();
        }

        Point projectedTopRight = projection.project(topRight);
        Point projectedBottomLeft = projection.project(bottomLeft);

        int width = (int) Math.round((projectedTopRight.x() - projectedBottomLeft.x())
                        / (projectedTopRight.y() - projectedBottomLeft.y())
                        * height);


        OSMToGeoTransformer osmToGeoTransformer = new OSMToGeoTransformer(projection);
        DigitalElevationModel dem = new HGTDigitalElevationModel(new File(args[1]));
        ReliefShader reliefShader = new ReliefShader(projection, dem, LIGHT_SOURCE);

        long startTime = System.nanoTime();
        System.out.print("Reading OSM file... ");
        OSMMap osmMap = OSMMapReader.readOSMFile(args[0], true);
        long elapsed = System.nanoTime() - startTime;
        System.out.printf("Finish reading OSM file in %.2f s\n", elapsed * 1e-9);
        startTime = System.nanoTime();
        System.out.print("Creating object map... ");
        Map map = osmToGeoTransformer.transform(osmMap);

        Grid grid = new Grid(projectedBottomLeft, projectedTopRight);
        map.addGrid(grid);

        Contours contours = new Contours(projection, dem, projectedBottomLeft, projectedTopRight,
                width / (5 * dpi / 100),
                height / (5 * dpi / 100));
        map.addContours(contours);

        elapsed = System.nanoTime() - startTime;
        System.out.printf("Object map done in %.2f s\n", elapsed * 1e-9);

        Java2DCanvas canvas = new Java2DCanvas(
                projectedBottomLeft, projectedTopRight,
                width, height, dpi, Color.WHITE);

        startTime = System.nanoTime();
        System.out.print("Drawing map... ");
        SwissPainter.painter().drawMap(map, canvas);
        elapsed = System.nanoTime() - startTime;
        System.out.printf("Drawing finished in %.2f s\n", elapsed * 1e-9);

        startTime = System.nanoTime();
        System.out.print("Generating Shadows... ");
        BufferedImage relief = reliefShader.shadedRelief(
                projectedBottomLeft, projectedTopRight, width, height,
                0.35f * 0.0017f * pixelPerMeterResolution);
        elapsed = System.nanoTime() - startTime;
        System.out.printf("Shadows finished in %.2f s\n", elapsed * 1e-9);

        dem.close();

        BufferedImage finalImage = combine(relief, canvas.image());
        finalImage = Border.drawBorder(finalImage);

        ImageIO.write(finalImage, "png", new File(args[7]));
    }

    private static BufferedImage combine(BufferedImage shadedRelief, BufferedImage plainMap) {
        int width = plainMap.getWidth();
        int height = plainMap.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color c = mult(Color.rgb(shadedRelief.getRGB(x, y)), Color.rgb(plainMap.getRGB(x, y)));
                result.setRGB(x, y, convert(c).getRGB());
            }
        }
        return result;
    }
}
