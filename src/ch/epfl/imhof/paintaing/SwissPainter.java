package ch.epfl.imhof.paintaing;

import static ch.epfl.imhof.paintaing.Color.gray;
import static ch.epfl.imhof.paintaing.Color.rgb;
import static ch.epfl.imhof.paintaing.Filters.tagged;
import static ch.epfl.imhof.paintaing.Painter.line;
import static ch.epfl.imhof.paintaing.Painter.outline;
import static ch.epfl.imhof.paintaing.Painter.polygon;
import static ch.epfl.imhof.paintaing.RoadPainterGenerator.RoadSpec;

public final class SwissPainter {
    private static final Painter PAINTER;

    static {
        Color black = Color.BLACK;
        Color gray = gray(0.5);
        Color red = Color.RED;
        Color darkBlue = rgb(0, 0.49, 0.77);
        Color brown = rgb(0.68, 0.43, 0.16);
        Color lightBrown = rgb(0.84, 0.85, 0.62);
        Color orange = rgb(1, 0.75, 0.2);
        Color lightOrange = rgb(1, 0.95, 0.4);
//        Color orange = rgb(0.97, 0.59, 0.12);
        Color lightRed = rgb(0.95, 0.7, 0.6);
//        Color lightRed = rgb(0.96, 0.60, 0.65);
        Color yellow = rgb(0.97, 0.91, 0);
        Color paleYellow = rgb(0.93, 0.94, 0.83);
        Color rose = rgb(0.76, 0.16, 0.46);
        Color lightRose = rgb(0.93, 0.66, 0.78);
        Color lightGreen = rgb(0.63, 0.81, 0.51);
        Color paleGreen = rgb(0.78, 0.84, 0.67);
        Color lightBlue = rgb(0.84, 0.91, 0.98);
        Color white = Color.WHITE;
        Color lightYellow = rgb(1, 0.93, 0);
        Color forestGreen = rgb(0.76, 0.88, 0.73);

        Color darkGray = gray(0.2);
        Color darkGreen = rgb(0.75, 0.85, 0.7);
        Color darkRed = rgb(0.7, 0.15, 0.15);
        Color lightGray = gray(0.9);

        Painter gridPainter = line(0.5f, gray).when(tagged("grid_line", "yes"));

        Painter roadPainter = RoadPainterGenerator.painterForRoads(
                new RoadSpec(tagged("highway", "motorway", "trunk"), 2, orange, 0.5f, black),
                new RoadSpec(tagged("highway", "primary"), 1.7f, lightRed, 0.35f, black),
                new RoadSpec(tagged("highway", "motorway_link", "trunk_link"), 1.7f, orange, 0.35f, black),
                new RoadSpec(tagged("highway", "secondary"), 1.7f, lightYellow, 0.35f, black),
                new RoadSpec(tagged("highway", "primary_link"), 1.7f, lightRed, 0.35f, black),
                new RoadSpec(tagged("highway", "tertiary"), 1.7f, white, 0.35f, lightRose),
                new RoadSpec(tagged("highway", "secondary_link"), 1.7f, lightYellow, 0.35f, black),
                new RoadSpec(tagged("highway", "residential", "living_street", "unclassified"), 1.2f, white, 0.15f, black),
                new RoadSpec(tagged("highway", "service", "pedestrian"), 0.5f, white, 0.15f, black));

        Painter fgPainter =
                gridPainter.above(roadPainter)
                .above(line(0.5f, black, LineStyle.LineCap.Round, LineStyle.LineJoin.Miter, 1f, 2f).when(tagged("highway", "footway", "steps", "path", "track", "cycleway")))
                .above(polygon(black).when(tagged("building")))
                .above(polygon(lightBlue).when(tagged("leisure", "swimming_pool")))
                .above(line(0.7f, darkRed).when(tagged("railway", "rail", "turntable")))
                .above(line(0.5f, darkRed).when(tagged("railway", "subway", "narrow_gauge", "light_rail")))
                .above(polygon(lightGreen).when(tagged("leisure", "pitch")))
                .above(line(1, black).when(tagged("man_made", "pier")))
                .above(line(0.5f, brown).when(tagged("major_contour")))
                .above(line(0.5f, orange).when(tagged("minor_contour")))
                .layered();

        Painter bgPainter =
                outline(1, darkBlue).above(polygon(lightBlue)).when(tagged("natural", "water").or(tagged("waterway", "riverbank")))
                .above(line(1, lightBlue).above(line(1.5f, darkBlue)).when(tagged("waterway", "river", "canal")))
                .above(line(1, darkBlue).when(tagged("waterway", "stream")))
                .above(polygon(forestGreen).when(tagged("natural", "wood").or(tagged("landuse", "forest"))))
                .above(polygon(lightBrown).when(tagged("natural", "heath")))
                .above(polygon(paleGreen).when(tagged("natural", "scrub")))
                .above(polygon(lightGreen).when(tagged("landuse", "grass", "recreation_ground", "meadow", "cemetery").or(tagged("leisure", "park"))))
                .above(polygon(paleYellow).when(tagged("landuse", "farmland")))
                .above(polygon(lightOrange).when(tagged("landuse", "farmyard")))
                .above(polygon(lightGray).when(tagged("landuse", "residential", "industrial")))
                .layered();

        PAINTER = fgPainter.above(bgPainter);
    }

    public static Painter painter() {
        return PAINTER;
    }
}
