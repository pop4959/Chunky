package org.popcraft.chunky.platform.util;

public class Vector2 {
    private double x;
    private double z;

    public Vector2(double x, double z) {
        this.x = x;
        this.z = z;
    }

    public static Vector2 of(double x, double z) {
        return new Vector2(x, z);
    }

    public Vector2 add(Vector2 other) {
        x += other.x;
        z += other.z;
        return this;
    }

    public Vector2 multiply(double value) {
        x *= value;
        z *= value;
        return this;
    }

    public Vector2 normalize() {
        final double length = length();
        x /= length;
        z /= length;
        return this;
    }

    public double distance(Vector2 other) {
        return Math.sqrt(distanceSquared(other));
    }

    public double distanceSquared(Vector2 other) {
        final double dx = this.x - other.x;
        final double dz = this.z - other.z;
        return dx * dx + dz * dz;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public double lengthSquared() {
        return x * x + z * z;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }
}
