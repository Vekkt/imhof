package ch.epfl.imhof.osm;

import ch.epfl.imhof.Attributes;
import ch.epfl.imhof.PointGeo;
import ch.epfl.imhof.Preconditions;

public final class OSMNode extends OSMEntity {
    private PointGeo position;

    public OSMNode(long id, PointGeo position, Attributes attributes) {
        super(id, attributes);
        this.position = position;
    }

    public PointGeo position() {
        return this.position;
    }

    public static final class Builder extends OSMEntity.Builder{
        private PointGeo position;

        public Builder(long id, PointGeo position) {
            super(id);
            this.position = position;
        }

        public final OSMNode build() {
            if (isIncomplete()) throw new IllegalStateException();
            return new OSMNode(super.id, this.position, super.attributesBuilder.build());
        }
    }
}
