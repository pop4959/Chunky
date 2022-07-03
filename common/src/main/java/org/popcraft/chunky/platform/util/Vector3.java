package org.popcraft.chunky.platform.util;

public class Vector3 {
    private double x;
    private double y;
    private double z;

    public Vector3(final double x, final double y, final double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vector3 of(final double x, final double y, final double z) {
        return new Vector3(x, y, z);
    }

    public Vector3 add(final Vector3 other) {
        x += other.x;
        y += other.y;
        z += other.z;
        return this;
    }

    public Vector3 multiply(final double value) {
        x *= value;
        y *= value;
        z *= value;
        return this;
    }

    public Vector3 normalize() {
        final double length = length();
        x /= length;
        y /= length;
        z /= length;
        return this;
    }

    public double distance(final Vector3 other) {
        return Math.sqrt(distanceSquared(other));
    }

    public double distanceSquared(final Vector3 other) {
        final double dx = this.x - other.x;
        final double dy = this.y - other.y;
        final double dz = this.z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public double lengthSquared() {
        return x * x + y * y + z * z;
    }

    public double getX() {
        return x;
    }

    public void setX(final double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(final double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(final double z) {
        this.z = z;
    }
}
