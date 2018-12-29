package ch.epfl.imhof.paintaing;

import ch.epfl.imhof.Attributed;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public final class Filters {
    private Filters() { }

    public static Predicate<Attributed<?>> tagged(String attributeName) {
        return (attributed -> attributed.hasAttribute(attributeName));
    }

    public static Predicate<Attributed<?>> tagged(String attributeName, String val, String... values) {
        Set<String> valueSet = new HashSet<>();
        valueSet.add(val);
        Collections.addAll(valueSet, values);

        return (attributed -> valueSet.contains(attributed.attributeValue(attributeName)));
    }

    public static Predicate<Attributed<?>> onLayer(int layer) {
        return (attributed -> attributed.attributeValue("layer", 0) == layer);
    }
}


