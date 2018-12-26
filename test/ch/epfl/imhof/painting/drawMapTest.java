package ch.epfl.imhof.painting;

import ch.epfl.imhof.Attributed;
import ch.epfl.imhof.Map;
import ch.epfl.imhof.geometry.Point;
import ch.epfl.imhof.osm.OSMMap;
import ch.epfl.imhof.osm.OSMMapReader;
import ch.epfl.imhof.osm.OSMToGeoTransformer;
import ch.epfl.imhof.paintaing.Color;
import ch.epfl.imhof.paintaing.Filters;
import ch.epfl.imhof.paintaing.Java2DCanvas;
import ch.epfl.imhof.paintaing.Painter;
import ch.epfl.imhof.projection.CH1903Projection;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.function.Predicate;

public class drawMapTest {

    @Test
    public void mapIsCorrectlyDrawn() throws IOException {
        // Le peintre et ses filtres
        Predicate<Attributed<?>> isLake =
                Filters.tagged("natural", "water");
        Painter lakesPainter =
                Painter.polygon(Color.BLUE).when(isLake);

        Predicate<Attributed<?>> isBuilding =
                Filters.tagged("building");
        Painter buildingsPainter =
                Painter.polygon(Color.BLACK).when(isBuilding);

        Painter painter = buildingsPainter.above(lakesPainter);

        OSMMap osmmap = OSMMapReader.readOSMFile("lausanne.osm.gz", true); // Lue depuis lausanne.osm.gz
        Map map = (new OSMToGeoTransformer(new CH1903Projection())).transform(osmmap);

        // La toile
        Point bl = new Point(532510, 150590);
        Point tr = new Point(539570, 155260);
        Java2DCanvas canvas =
                new Java2DCanvas(bl, tr, 800, 530, 72, Color.WHITE);

        // Dessin de la carte et stockage dans un fichier
        painter.drawMap(map, canvas);
        ImageIO.write(canvas.image(), "png", new File("loz.png"));
    }
}
