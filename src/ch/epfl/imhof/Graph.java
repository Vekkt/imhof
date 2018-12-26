package ch.epfl.imhof;

import ch.epfl.imhof.osm.OSMNode;

import java.util.*;
import java.util.Map;

public final class Graph<N> {
    private final Map<N, Set<N>> neighbors;

    public Graph(Map<N, Set<N>> neighbors) {
        Map<N, Set<N>> tmp = new HashMap<>();

        for (Map.Entry<N, Set<N>> mapEntry : neighbors.entrySet())
            tmp.put(mapEntry.getKey(), Collections.unmodifiableSet(new HashSet<>(mapEntry.getValue())));

        this.neighbors = Collections.unmodifiableMap(tmp);
    }

    public Set<N> nodes() {
        return this.neighbors.keySet();
    }

    public Set<N> neighborsOf(N node) {
        Preconditions.checkArgument(this.neighbors.containsKey(node));
        return this.neighbors.get(node);
    }

    public static final class Builder<N> {
        private final Map<N, Set<N>> neighbors = new HashMap<>();

        public void addNode(N n) {
            if (!neighbors.containsKey(n))
                this.neighbors.put(n, new HashSet<>());
        }

        public void addEdge(N n1, N n2) {
            Preconditions.checkArgument(this.neighbors.containsKey(n1));
            Preconditions.checkArgument(this.neighbors.containsKey(n2));

            Set<N> newNeighbors = this.neighbors.get(n1);
            newNeighbors.add(n2);
            this.neighbors.put(n1, newNeighbors);

            newNeighbors = this.neighbors.get(n2);
            newNeighbors.add(n1);
            this.neighbors.put(n2, newNeighbors);
        }

        public Graph<N> build() {
            Map<N, Set<N>> safeMap = new HashMap<>();
            for (N key: neighbors.keySet())
                safeMap.put(key, Collections.unmodifiableSet(new HashSet<>(neighbors.get(key))));
            return new Graph<>(Collections.unmodifiableMap(safeMap));
        }
    }
}
