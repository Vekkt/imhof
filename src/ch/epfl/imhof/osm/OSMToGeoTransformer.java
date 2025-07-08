package ch.epfl.imhof.osm;

import ch.epfl.imhof.*;
import ch.epfl.imhof.Map;
import ch.epfl.imhof.geometry.ClosedPolyLine;
import ch.epfl.imhof.geometry.OpenPolyLine;
import ch.epfl.imhof.geometry.PolyLine;
import ch.epfl.imhof.geometry.Polygon;
import ch.epfl.imhof.projection.Projection;

import java.util.*;

public final class OSMToGeoTransformer {
    private final Projection projection;

    private static final Set<String> AREA_ATTS = new HashSet<>(
    Arrays.asList(
        "aeroway", "amenity", "building", "harbour", "historic",
        "landuse", "leisure", "man_made", "military", "natural",
        "office", "place", "power", "public_transport", "shop",
        "sport", "tourism", "water", "waterway", "wetland"
    ));

    private static final Set<String> POLYLINE_ATTS = new HashSet<>(
    Arrays.asList(
            "bridge", "highway", "layer", "man_made", "railway",
            "tunnel", "waterway", "grid_line"
    ));

    private static final Set<String> POLYGON_ATTS = new HashSet<>(
    Arrays.asList(
            "building", "landuse", "layer", "leisure", "natural",
            "waterway"
    ));


    public OSMToGeoTransformer(Projection projection) {
        this.projection = projection;
    }

    public Map transform(OSMMap map) {
        Map.Builder mapBuilder = new Map.Builder();
        Attributes filteredAttributes;

        PolyLine.Builder polyLineBuilder;
        for (OSMWay way: map.ways()) {
            polyLineBuilder = new PolyLine.Builder();

            for (OSMNode n: way.nonRepeatingNodes())
                polyLineBuilder.addPoint(projection.project(n.position()));

            if (way.isClosed() && isSurface(way)) {
                filteredAttributes = way.attributes().keepOnlyKeys(POLYGON_ATTS);
                if (!filteredAttributes.isEmpty())
                    mapBuilder.addPolygon(new Attributed<>(new Polygon(
                            polyLineBuilder.buildClosed()), filteredAttributes));
            } else {
                filteredAttributes = way.attributes().keepOnlyKeys(POLYLINE_ATTS);
                if (!filteredAttributes.isEmpty())
                    mapBuilder.addPolyLine(new Attributed<>(
                            polyLineBuilder.buildOpen(), filteredAttributes));
            }
        }

        for (OSMRelation relation: map.relations()) {
            if (relation.hasAttribute("type") && relation.attributeValue("type").equals("multipolygon")) {
                filteredAttributes = relation.attributes().keepOnlyKeys(POLYGON_ATTS);
                if (!filteredAttributes.isEmpty()) {
                    assemblePolygon(relation, filteredAttributes).forEach(mapBuilder::addPolygon);
                }
            }
        }
        return mapBuilder.build();
    }

    private boolean isSurface(OSMWay way) {
        if (!way.attributes().keepOnlyKeys(AREA_ATTS).isEmpty())
            return true;

        return way.hasAttribute("area")
                && (way.attributeValue("area").equals("1")
                || way.attributeValue("area").equals("yes")
                || way.attributeValue("area").equals("true"));
    }

    private List<ClosedPolyLine> ringsForRole(OSMRelation relation, String role) {
        List<OSMWay> ways = new ArrayList<>();

        for (OSMRelation.Member m: relation.members()) {
            if (m.role().equals(role) && m.type() == OSMRelation.Member.Type.WAY)
                ways.add((OSMWay) m.member());
        }

        return getRingsFromGraph(makeGraph(ways));
    }

    private List<ClosedPolyLine> getRingsFromGraph(Graph<OSMNode> graph) {
        List<ClosedPolyLine> rings = new ArrayList<>();
        Set<OSMNode> toVisit = new HashSet<>(graph.nodes());
        Set<OSMNode> neighbors;
        PolyLine.Builder polyLineBuilder;
        OSMNode currentNode;

        while (toVisit.iterator().hasNext()) {
            polyLineBuilder = new PolyLine.Builder();
            currentNode = toVisit.iterator().next();

            polyLineBuilder.addPoint(projection.project(currentNode.position()));
            toVisit.remove(currentNode);

            do {
                neighbors = new HashSet<>(graph.neighborsOf(currentNode));
                if (neighbors.size() != 2) return Collections.emptyList();

                neighbors.retainAll(toVisit);
                if (neighbors.iterator().hasNext()) {
                    currentNode = neighbors.iterator().next();
                    polyLineBuilder.addPoint(projection.project(currentNode.position()));
                    toVisit.remove(currentNode);
                }
            } while (neighbors.iterator().hasNext());

            rings.add(polyLineBuilder.buildClosed());
        }
        return rings;
    }

    private List<Attributed<Polygon>> assemblePolygon(OSMRelation relation, Attributes attributes) {
        List<Attributed<Polygon>> polygons = new ArrayList<>();
        List<ClosedPolyLine> innerRings = ringsForRole(relation, "inner");
        List<ClosedPolyLine> outerRings = ringsForRole(relation, "outer");
        List<ClosedPolyLine> holes;

        outerRings.sort(Comparator.comparing(ClosedPolyLine::area));

        for (ClosedPolyLine or: outerRings) {
            holes = new ArrayList<>();

            for (ClosedPolyLine ir: innerRings)
                if (or.containsPoint(ir.firstPoint()))
                    holes.add(ir);

            if (holes.isEmpty())
                polygons.add(new Attributed<>(new Polygon(or), attributes));
            else
                polygons.add(new Attributed<>(new Polygon(or, holes), attributes));
            innerRings.removeAll(holes);
        }
        return polygons;
    }

    private Graph<OSMNode> makeGraph(List<OSMWay> ways) {
        Graph.Builder<OSMNode> graphBuilder = new Graph.Builder<>();

        for (OSMWay w: ways) {
            for (int i = 0; i < w.nodes().size() - 1; i++) {
                OSMNode n1 = w.nodes().get(i);
                OSMNode n2 = w.nodes().get(i + 1);

                graphBuilder.addNode(n1);
                graphBuilder.addNode(n2);

                graphBuilder.addEdge(n1, n2);
            }
        }
        return graphBuilder.build();
    }
}
