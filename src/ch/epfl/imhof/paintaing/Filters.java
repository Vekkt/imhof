package ch.epfl.imhof.paintaing;

import ch.epfl.imhof.Attributed;

import java.util.function.Predicate;

public final class Filters {
    private Filters() { }

    public static Predicate<Attributed<?>> tagged(String attributeName) {
        return (attributed -> attributed.hasAttribute(attributeName));
    }

    public static Predicate<Attributed<?>> tagged(String attributeName, String... values) {
        return (attributed -> attributed.hasAttribute(attributeName)
        && hasValue(attributed, attributeName, values));
    }

    public static Predicate<Attributed<?>> onLayer(int layer) {
        return (attributed -> attributed.hasAttribute("layer")
        && attributed.attributes().get("layer", 0) == layer);
    }

    private static boolean hasValue(Attributed<?> attributed, String attributeName, String... values) {
        for (String val: values)
            if (attributed.attributeValue(attributeName, null).equals(val))
                return true;
        return false;
    }
}


