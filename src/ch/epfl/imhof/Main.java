package ch.epfl.imhof;

import ch.epfl.imhof.dem.DigitalElevationModel;
import ch.epfl.imhof.dem.Earth;
import ch.epfl.imhof.dem.HGTDigitalElevationModel;
import ch.epfl.imhof.dem.ReliefShader;
import ch.epfl.imhof.geometry.Point;
import ch.epfl.imhof.osm.OSMMap;
import ch.epfl.imhof.osm.OSMMapReader;
import ch.epfl.imhof.osm.OSMToGeoTransformer;
import ch.epfl.imhof.paintaing.Color;
import ch.epfl.imhof.paintaing.Java2DCanvas;
import ch.epfl.imhof.paintaing.SwissPainter;
import ch.epfl.imhof.projection.CH1903Projection;
import ch.epfl.imhof.projection.EquirectangularProjection;
import ch.epfl.imhof.projection.Projection;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static ch.epfl.imhof.paintaing.Color.convert;
import static ch.epfl.imhof.paintaing.Color.mult;

public final class Main {
    private final static Vector3 LIGHT_SOURCE = new Vector3(-1, 1, 1);


    public static void main(String[] args) throws Exception {
        PointGeo topRight = new PointGeo(Math.toRadians(Double
                .parseDouble(args[4])), Math.toRadians(Double
                .parseDouble(args[5])));
        PointGeo bottomLeft = new PointGeo(Math.toRadians(Double
                .parseDouble(args[2])), Math.toRadians(Double
                .parseDouble(args[3])));


        int dpi = Integer.parseInt(args[6]);
        int pixelPerMeterResolution = (int) Math.round(dpi * (5000d / 127d));


        int height = (int) Math.round(pixelPerMeterResolution
                * (topRight.latitude() - bottomLeft.latitude()) * Earth.RADIUS
                / 25000d);


        Projection projection = null;

        if (args.length > 8) {
            switch (args[8]) {
                case "CH1903":
                    projection = new CH1903Projection();
                    break;
                case "Equirectangular":
                    projection = new EquirectangularProjection();
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Nom de projection invalide.");
            }
        } else {
            projection = new CH1903Projection();
        }

        Point projectedTopRight = projection.project(topRight);
        Point projectedBottomLeft = projection.project(bottomLeft);

        int width = (int) Math
                .round((projectedTopRight.x() - projectedBottomLeft.x())
                        / (projectedTopRight.y() - projectedBottomLeft.y())
                        * height);


        OSMMap osmMap;
        osmMap = OSMMapReader.readOSMFile(args[0], true);
        OSMToGeoTransformer osmToGeoTransformer = new OSMToGeoTransformer(
                projection);
        Map map = osmToGeoTransformer.transform(osmMap);
        Java2DCanvas canvas = new Java2DCanvas(projectedBottomLeft,
                projectedTopRight, width, height, dpi, Color.WHITE);
        SwissPainter.painter().drawMap(map, canvas);

        DigitalElevationModel dem = new HGTDigitalElevationModel(new File(args[1]));

        ReliefShader reliefShader = new ReliefShader(projection, dem, LIGHT_SOURCE);

        BufferedImage relief = reliefShader.shadedRelief(projectedBottomLeft,
                projectedTopRight, width, height,
                0.0017f * pixelPerMeterResolution);

        dem.close();

        BufferedImage finalImage = combine(relief, canvas.image());

        ImageIO.write(finalImage, "png", new File(args[7]));
    }

    private static BufferedImage combine(BufferedImage shadedRelief,
                                         BufferedImage plainMap) {
        int width = plainMap.getWidth();
        int height = plainMap.getHeight();
        BufferedImage result = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color c = mult(Color.rgb(shadedRelief.getRGB(x, y)), Color.rgb(plainMap.getRGB(x, y)));
                result.setRGB(x, y, convert(c).getRGB());
            }
        }
        return result;
    }
}
