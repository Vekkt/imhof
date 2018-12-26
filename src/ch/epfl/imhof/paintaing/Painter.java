package ch.epfl.imhof.paintaing;

import ch.epfl.imhof.Attributed;
import ch.epfl.imhof.Map;
import ch.epfl.imhof.geometry.PolyLine;
import ch.epfl.imhof.geometry.Polygon;

import java.util.function.Predicate;

public interface Painter {
    void drawMap(Map m, Canvas c);

    static Painter polygon(Color c) {
        return (m, cvs) -> {
            for (Attributed<Polygon> attrP : m.polygons())
                cvs.drawPolygon(attrP.value(), c);
        };
    }

    static Painter outline(
            int lineWidth,
            Color lineColor,
            LineStyle.Cap lineCap,
            LineStyle.Join lineJoin,
            float... dashingPattern) {

        return Painter.outline(new LineStyle(
                lineColor,
                lineCap,
                lineJoin,
                lineWidth,
                dashingPattern));
    }

    static Painter outline(float lineWidth, Color lineColor) {
        return Painter.outline(new LineStyle(lineColor, lineWidth));
    }

    static Painter outline(LineStyle s) {
        return (m, cvs) -> {
            for (Attributed<Polygon> attrP : m.polygons()) {
                cvs.drawPolyLine(attrP.value().shell(), s);
                for (PolyLine p : attrP.value().holes())
                    cvs.drawPolyLine(p, s);
            }
        };
    }

    static Painter line(float lineWidth,
                        Color lineColor,
                        LineStyle.Cap lineCap,
                        LineStyle.Join lineJoin,
                        float... dashingPattern) {

        return Painter.line(new LineStyle(
                lineColor,
                lineCap,
                lineJoin,
                lineWidth,
                dashingPattern));
    }

    static Painter line(float lineWidth, Color lineColor) {
        return Painter.line(new LineStyle(lineColor, lineWidth));
    }

    static Painter line(LineStyle s) {
        return (m, cvs) -> {
            for (Attributed<PolyLine> attrPL : m.polyLines())
                cvs.drawPolyLine(attrPL.value(), s);
        };
    }

    default Painter when(Predicate<Attributed<?>> predicate) {
        return (m, cvs) -> {
            Map.Builder mapB = new Map.Builder();
            m.polygons().stream().filter(predicate).forEach(mapB::addPolygon);
            m.polyLines().stream().filter(predicate).forEach(mapB::addPolyLine);
            drawMap(mapB.build(), cvs);
        };
    }

    default Painter above(Painter that) {
        return (m, cvs) -> {
            that.drawMap(m, cvs);
            this.drawMap(m, cvs);
        };
    }

    default Painter layered() {
        Painter p = when(Filters.onLayer(-5));
        for (int i = -4; i <= 5; i++)
            p = when(Filters.onLayer(i)).above(p);

        return p;
    }

}
