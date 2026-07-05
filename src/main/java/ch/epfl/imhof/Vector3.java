package ch.epfl.imhof;

public record Vector3(double x, double y, double z) {

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

    public Vector3 sum(Vector3 that) {
        return new Vector3(this.x + that.x, this.y + that.y, this.z + that.y);
    }

    public Vector3 sub(Vector3 that) {
        return new Vector3(this.x - that.x, this.y - that.y, this.z - that.y);
    }

    public Vector3 prod(Vector3 that) {
        return new Vector3(
                this.y * that.z - this.z * that.y,
                this.z * that.x - this.x * that.z,
                this.x * that.y - this.y * that.x
        );
    }

    public Vector3 mul(double scalar) {
        return new Vector3(this.x * scalar, this.y * scalar, this.x * scalar);
    }

    public Vector3 div(double scalar) {
        Preconditions.checkArgument(scalar != 0, "Illegal division by 0.");
        return this.mul(1 / scalar);
    }


}
