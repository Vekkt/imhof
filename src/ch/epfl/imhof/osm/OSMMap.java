package ch.epfl.imhof.osm;

import java.util.*;

public final class OSMMap {
    private final List<OSMWay> ways;
    private final List<OSMRelation> relations;

    public OSMMap(Collection<OSMWay> ways, Collection<OSMRelation> relations) {
        this.ways = Collections.unmodifiableList(new ArrayList<>(ways));
        this.relations = Collections.unmodifiableList(new ArrayList<>(relations));
    }

    public List<OSMWay> ways() {
        return this.ways;
    }

    public List<OSMRelation> relations() {
        return this.relations;
    }

    public static final class Builder {
        private final Map<Long, OSMNode> nodes;
        private final Map<Long, OSMWay> ways;
        private final Map<Long, OSMRelation> relations;

        public Builder() {
            nodes = new HashMap<>();
            ways = new HashMap<>();
            relations = new HashMap<>();
        }

        public void addNode(OSMNode newNode) {
            this.nodes.put(newNode.id(), newNode);
        }

        public OSMNode nodeForId(long id) {
            return this.nodes.getOrDefault(id, null);
        }

        public void addWay(OSMWay newWay) {
            this.ways.put(newWay.id(), newWay);
        }

        public OSMWay wayForId(long id) {
            return this.ways.getOrDefault(id, null);
        }

        public void addRelation(OSMRelation newRelation) {
            this.relations.put(newRelation.id(), newRelation);
        }

        public OSMRelation relationForId(long id) {
            return this.relations.getOrDefault(id, null);
        }

        public OSMMap build() {
            return new OSMMap(this.ways.values(), this.relations.values());
        }
    }
}
