package io.github.wysohn.triggerreactor.core.manager.location;

public class Vector {
    private final double x;
    private final double y;
    private final double z;

    public Vector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private Vector(boolean unit, double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Vector vector = (Vector) o;

        if (Double.compare(vector.x, x) != 0)
            return false;
        if (Double.compare(vector.y, y) != 0)
            return false;
        return Double.compare(vector.z, z) == 0;
    }

    @Override
    public String toString() {
        return "Vector{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
    }

    public Vector add(Vector v) {
        return add(v.x, v.y, v.z);
    }

    public Vector add(double x, double y, double z) {
        return new Vector(this.x + x, this.y + y, this.z + z);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public boolean isZero() {
        return x == 0.0 && y == 0.0 && z == 0.0;
    }

    /**
     * Matrix multiplication (this^T . V)
     *
     * @param v
     * @return
     */
    public Vector mult(Vector v) {
        return mult(v.x, v.y, v.z);
    }

    public Vector mult(double x, double y, double z) {
        return new Vector(this.x * x, this.y * y, this.z * z);
    }

    public Vector mult(double scalar) {
        return new Vector(scalar * x, scalar * y, scalar * z);
    }

    public Vector unit() {
        double normalizationFactor = Math.sqrt(x * x + y * y + z * z);
        if (normalizationFactor == 0.0)
            throw new RuntimeException("Normalization factor cannot be 0. (Perhaps it's a zero vector?)");

        return new Vector(x / normalizationFactor, y / normalizationFactor, z / normalizationFactor);
    }

    public static Vector zero() {
        return new Vector(0.0, 0.0, 0.0);
    }
}