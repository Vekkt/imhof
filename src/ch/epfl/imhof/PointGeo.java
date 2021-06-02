package ch.epfl.imhof;

import ch.epfl.imhof.dem.Earth;

public final class PointGeo {
    private double longitude;
    private double latitude;

    public PointGeo(double longitude, double latitude) {
        Preconditions.inCloseBounds(-Math.PI,longitude, Math.PI);
        Preconditions.inCloseBounds(-Math.PI/2, latitude, Math.PI/2);

        this.longitude = longitude;
        this.latitude = latitude;
    }

    public final double longitude() {
        return this.longitude;
    }

    public final double latitude() {
        return this.latitude;
    }

    public final double dist(PointGeo that) {
        double r = Earth.RADIUS;
        double sPhi = Math.pow(Math.sin((that.latitude - this.latitude) / 2), 2);
        double sLam = Math.pow(Math.sin((that.longitude - this.longitude()) / 2), 2);
        double cLam = Math.cos(this.longitude) * Math.cos(that.longitude);
        return 2 * r * Math.asin(Math.sqrt(sPhi + cLam * sLam));
    }


}
