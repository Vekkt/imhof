package ch.epfl.imhof.projection;

import ch.epfl.imhof.geometry.Point;
import ch.epfl.imhof.PointGeo;

public interface Projection {

    Point project(PointGeo point);
    PointGeo inverse(Point point);

}
