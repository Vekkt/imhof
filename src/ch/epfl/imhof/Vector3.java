package ch.epfl.imhof;

public class Vector3 {
    private final double x;
    private final double y;
    private final double z;
    
    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public double norm() {
        return Math.sqrt(this.scalarProduct(this));
    }

    public Vector3 normalized() {
        double norm = this.norm();
        return new Vector3(x / norm, y / norm, z / norm);
    }

    public double scalarProduct(Vector3 that) {
        return x * that.x + y * that.y + z * that.z;
    }
    
    
}
