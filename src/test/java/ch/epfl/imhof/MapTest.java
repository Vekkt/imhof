package ch.epfl.imhof;

import ch.epfl.imhof.geometry.Point;
import ch.epfl.imhof.geometry.PolyLine;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapTest {
    @Test
    void constructorDefensivelyCopiesCollections() {
        List<Attributed<PolyLine>> sourcePolyLines = new ArrayList<>();
        Map map = new Map(sourcePolyLines, List.of());

        sourcePolyLines.add(polyLine());

        assertTrue(map.polyLines().isEmpty());
    }

    @Test
    void accessorsExposeUnmodifiableCollections() {
        Map map = new Map(List.of(), List.of());

        assertThrows(UnsupportedOperationException.class, () -> map.polyLines().add(polyLine()));
        assertThrows(UnsupportedOperationException.class, () -> map.polygons().clear());
    }

    @Test
    void withAdditionalPolyLinesReturnsNewMap() {
        Map map = new Map(List.of(), List.of());
        Map extendedMap = map.withAdditionalPolyLines(List.of(polyLine()));

        assertTrue(map.polyLines().isEmpty());
        assertEquals(1, extendedMap.polyLines().size());
    }

    private static Attributed<PolyLine> polyLine() {
        PolyLine.Builder builder = new PolyLine.Builder();
        builder.addPoint(new Point(0, 0));
        builder.addPoint(new Point(1, 1));
        return new Attributed<>(
                builder.buildOpen(),
                new Attributes(Collections.singletonMap("highway", "path")));
    }
}
