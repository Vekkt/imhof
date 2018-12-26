package ch.epfl.imhof.projection;

import ch.epfl.imhof.geometry.Point;
import ch.epfl.imhof.PointGeo;

public final class CH1903Projection implements Projection {

    public final Point project(PointGeo point) {
        double lambda = (Math.toDegrees(point.longitude()) * 3600.0 - 26782.5) / 10000.0;
        double phi = (Math.toDegrees(point.latitude()) * 3600.0 - 169028.66) / 10000.0;

        double x = 600072.37
                + 211455.93 * lambda
                - 10938.51 * lambda * phi
                - 0.36 * lambda * Math.pow(phi, 2)
                - 44.54 *  Math.pow(lambda, 3);

        double y = 200147.07
                + 308807.95 * phi
                + 3745.25 * Math.pow(lambda, 2)
                + 76.63 * Math.pow(phi, 2)
                - 194.56 * Math.pow(lambda, 2) * phi
                + 119.79 * Math.pow(phi, 3);

        return new Point(x, y);
    }

    public final PointGeo inverse(Point point) {
        double x = (point.x() - 600000.0) / 1000000.0;
        double y = (point.y() - 200000.0) / 1000000.0;

        double lambda = 2.6779094
                + 4.728982 * x
                + 0.791484 * x * y
                + 0.1306 * x * Math.pow(y, 2)
                - 0.0436 * Math.pow(x, 3);

        double phi = 16.9023892
                + 3.238272 * y
                - 0.270978 * Math.pow(x, 2)
                - 0.002528 * Math.pow(y, 2)
                - 0.0447 * Math.pow(x, 2) * y
                - 0.0140 * Math.pow(y, 3);

        lambda = Math.toRadians(lambda * 100.0 / 36.0);
        phi = Math.toRadians(phi * 100.0 / 36.0);

        return new PointGeo(lambda, phi);
    }

}
