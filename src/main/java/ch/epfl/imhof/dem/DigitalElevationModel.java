package ch.epfl.imhof.dem;

import ch.epfl.imhof.PointGeo;
import ch.epfl.imhof.Vector3;

import java.io.Closeable;

public interface DigitalElevationModel extends Closeable {
    Vector3 normalAt(PointGeo p) throws IllegalArgumentException;
    short bufferAt(PointGeo p) throws IllegalArgumentException;
}
