package ch.epfl.imhof;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class Attributes {
    private final Map<String, String> attributes;

    public Attributes(Map<String, String> attributes) {
        this.attributes = new HashMap<>(attributes);
    }

    public boolean isEmpty() {
        return this.attributes.isEmpty();
    }

    public boolean contains(String key) {
        return this.attributes.containsKey(key);
    }

    public String get(String key) {
        return this.attributes.getOrDefault(key, null);
    }

    public String get(String key, String defaultValue) {
        return this.attributes.getOrDefault(key, defaultValue);
    }

    public int get(String key, int defaultValue) {
        try {
            return Integer.parseInt(this.attributes.getOrDefault(key, Integer.toString(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public Attributes keepOnlyKeys(Set<String> keysToKeep) {
        HashMap<String, String> res = new HashMap<>();

        for (String entry : keysToKeep) {
            if (this.contains(entry))
                res.put(entry, get(entry));
        }
        return new Attributes(res);
    }

    public final static class Builder {
        private final Map<String, String> attributes = new HashMap<>();

        public void put(String key, String value) {
            attributes.put(key, value);
        }

        public Attributes build() {
            return new Attributes(attributes);
        }
    }
}
