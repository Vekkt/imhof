package ch.epfl.imhof;

import ch.epfl.imhof.geometry.PolyLine;
import ch.epfl.imhof.geometry.Polygon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Map {
    private List<Attributed<PolyLine>> polyLines;
    private List<Attributed<Polygon>> polygons;

    public Map(List<Attributed<PolyLine>> polyLines, List<Attributed<Polygon>> polygons) {
        this.polyLines = new ArrayList<>(Collections.unmodifiableList(polyLines));
        this.polygons = new ArrayList<>(Collections.unmodifiableList(polygons));
    }

    public List<Attributed<PolyLine>> polyLines() {
        return new ArrayList<>(Collections.unmodifiableList(this.polyLines));
    }

    public List<Attributed<Polygon>> polygons() {
        return new ArrayList<>(Collections.unmodifiableList(this.polygons));
    }

    public static final class Builder {
        private List<Attributed<PolyLine>> polyLines = new ArrayList<>();
        private List<Attributed<Polygon>> polygons = new ArrayList<>();

        public void addPolyLine(Attributed<PolyLine> newPolyLine) {
            this.polyLines.add(newPolyLine);
        }

        public void addPolygon(Attributed<Polygon> newPolygon) {
            this.polygons.add(newPolygon);
        }

        public Map build() {
            return new Map(this.polyLines, this.polygons);
        }
    }
}
