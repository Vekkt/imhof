package ch.epfl.imhof.geometry;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public final class Polygon {
    private final ClosedPolyLine shell;
    private final List<ClosedPolyLine> holes;


    public Polygon(ClosedPolyLine shell, List<ClosedPolyLine> holes) {
        this.shell = shell;
        this.holes = new ArrayList<>(holes);
    }

    public Polygon(ClosedPolyLine shell) {
        this.shell = shell;
        this.holes = new ArrayList<>();
    }

    public ClosedPolyLine shell() {
        return this.shell;
    }

    public List<ClosedPolyLine> holes() {
        return Collections.unmodifiableList(new ArrayList<>(this.holes));
    }
}
