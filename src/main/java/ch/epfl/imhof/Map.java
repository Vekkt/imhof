package ch.epfl.imhof;

import ch.epfl.imhof.geometry.PolyLine;
import ch.epfl.imhof.geometry.Polygon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record Map(List<Attributed<PolyLine>> polyLines, List<Attributed<Polygon>> polygons) {
    public Map(List<Attributed<PolyLine>> polyLines, List<Attributed<Polygon>> polygons) {
        this.polyLines = List.copyOf(polyLines);
        this.polygons = List.copyOf(polygons);
    }

    public Map withAdditionalPolyLines(Collection<Attributed<PolyLine>> additionalPolyLines) {
        List<Attributed<PolyLine>> combinedPolyLines = new ArrayList<>(polyLines);
        combinedPolyLines.addAll(additionalPolyLines);
        return new Map(combinedPolyLines, polygons);
    }

    public static final class Builder {
        private final List<Attributed<PolyLine>> polyLines = new ArrayList<>();
        private final List<Attributed<Polygon>> polygons = new ArrayList<>();

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
