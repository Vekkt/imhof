package ch.epfl.imhof;

public final class Attributed<T> {
    private final T value;
    private final Attributes attributes;

    public Attributed(T value, Attributes attributes) {
        this.value = value;
        this.attributes = attributes;
    }

    public T value() {
        return this.value;
    }

    public Attributes attributes() {
        return this.attributes;
    }

    public boolean hasAttribute(String attributeName) {
        return this.attributes().contains(attributeName);
    }

    public String attributeValue(String attributeName) {
        return this.attributes().get(attributeName);
    }

    public String attributeValue(String attributeName, String defaultValue) {
        return this.attributes().get(attributeName, defaultValue);
    }

    public int attributeValue(String attributeName, int defaultValue) {
        return this.attributes().get(attributeName, defaultValue);
    }
}
