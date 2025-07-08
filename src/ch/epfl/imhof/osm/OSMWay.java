package ch.epfl.imhof.osm;

import ch.epfl.imhof.Attributes;
import ch.epfl.imhof.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class OSMWay extends OSMEntity {
    private final List<OSMNode> nodes;

    public OSMWay(long id, List<OSMNode> nodes, Attributes attributes) {
        super(id, attributes);

        Preconditions.checkArgument(nodes.size() >= 2);
        this.nodes = new ArrayList<>(nodes);
    }

    public int nodesCount() {
        return this.nodes.size();
    }

    public List<OSMNode> nodes() {
        return new ArrayList<>(Collections.unmodifiableList(this.nodes));
    }

    public List<OSMNode> nonRepeatingNodes() {
        if (this.isClosed())
            return new ArrayList<>(Collections.unmodifiableList(this.nodes.subList(0, this.nodesCount() - 1)));
        return new ArrayList<>(Collections.unmodifiableList(this.nodes));
    }

    public OSMNode firstNode() {
        return this.nodes.get(0);
    }

    public OSMNode lastNode() {
        return this.nodes.get(this.nodesCount()-1);
    }

    public boolean isClosed() {
        return this.nodes.get(0).equals(this.nodes.get(this.nodesCount() - 1));
    }

    public static final class Builder extends OSMEntity.Builder {
        private final List<OSMNode> nodes = new ArrayList<>();

        public Builder(long id) {
            super(id);
        }

        public void addNode(OSMNode newNode) {
            this.nodes.add(newNode);
        }

        public boolean isIncomplete() {
            return this.nodes.size() < 2 || super.isIncomplete();
        }

        public OSMWay build() {
            if (isIncomplete()) throw new IllegalStateException();
            return new OSMWay(super.id, this.nodes, super.attributesBuilder.build());
        }
    }
}
