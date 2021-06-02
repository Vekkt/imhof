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

    public double x() { return x; }

    public double y() { return y; }

    public double z() { return z; }
    
    public final double norm() {
        return Math.sqrt(this.scalarProduct(this));
    }

    public final Vector3 normalized() {
        double norm = this.norm();
        return new Vector3(x / norm, y / norm, z / norm);
    }

    public final double scalarProduct(Vector3 that) {
        return x * that.x + y * that.y + z * that.z;
    }

    public final Vector3 sum(Vector3 that) {
        return new Vector3(this.x + that.x, this.y + that.y, this.z + that.y);
    }

    public final Vector3 sub(Vector3 that) {
        return new Vector3(this.x - that.x, this.y - that.y, this.z - that.y);
    }

    public final Vector3 prod(Vector3 that) {
        return new Vector3(
                this.y * that.z - this.z * that.y,
                this.z * that.x - this.x * that.z,
                this.x * that.y - this.y * that.x
        );
    }

    public final Vector3 mul(double scalar) {
        return new Vector3(this.x * scalar, this.y * scalar, this.x * scalar);
    }

    public final Vector3 div(double scalar) {
        Preconditions.checkArgument(scalar != 0, "Illegal division by 0.");
        return this.mul(1 / scalar);
    }
    
    
}
