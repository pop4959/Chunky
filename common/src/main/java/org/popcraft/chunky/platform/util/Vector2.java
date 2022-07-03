package org.popcraft.chunky.platform.util;

public class Vector2 {
    private double x;
    private double z;

    public Vector2(final double x, final double z) {
        this.x = x;
        this.z = z;
    }

    public static Vector2 of(final double x, final double z) {
        return new Vector2(x, z);
    }

    public Vector2 add(final Vector2 other) {
        x += other.x;
        z += other.z;
        return this;
    }

    public Vector2 multiply(final double value) {
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

    public double distance(final Vector2 other) {
        return Math.sqrt(distanceSquared(other));
    }

    public double distanceSquared(final Vector2 other) {
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

    public void setX(final double x) {
        this.x = x;
    }

    public double getZ() {
        return z;
    }

    public void setZ(final double z) {
        this.z = z;
    }
}
