package ch.epfl.imhof.osm;

import ch.epfl.imhof.Attributes;

import java.util.HashMap;

public abstract class OSMEntity {
    private Attributes attributes;
    private long id;

    public OSMEntity(long id, Attributes attributes) {
        this.attributes = attributes;
        this.id =  id;
    }

    public long id() {
        return this.id;
    }

    public Attributes attributes() {
        return this.attributes;
    }

    public boolean hasAttribute(String key) {
        return this.attributes.contains(key);
    }

    public String attributeValue(String key) {
        return this.attributes.get(key);
    }

    public static class Builder {
        private boolean incomplete;
        protected Attributes.Builder attributesBuilder;
        protected long id;

        public Builder(long id) {
            attributesBuilder = new Attributes.Builder();
            this.incomplete = false;
            this.id = id;
        }

        public void setAttribute(String key, String value) {
            this.attributesBuilder.put(key, value);
        }

        public void setIncomplete() {
            this.incomplete = true;
        }

        public boolean isIncomplete() {
            return incomplete;
        }
    }
}
