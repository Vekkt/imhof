package ch.epfl.imhof;

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


}
