package org.popcraft.chunky.platform.util;

import org.popcraft.chunky.platform.World;

public class Location {
    private World world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public Location(World world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Location(World world, double x, double y, double z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3 toVector() {
        return new Vector3(x, y, z);
    }

    public Location add(Vector3 vector) {
        x += vector.getX();
        y += vector.getY();
        z += vector.getZ();
        return this;
    }

    public Location setDirection(Vector3 direction) {
        final double dirX = direction.getX();
        final double dirY = direction.getY();
        final double dirZ = direction.getZ();
        if (dirX == 0 && dirZ == 0) {
            if (dirY == 0) {
                pitch = 0;
            } else {
                pitch = dirY < 0 ? 90 : -90;
            }
            return this;
        }
        yaw = (float) Math.toDegrees(Math.atan2(-dirX, dirZ));
        pitch = (float) Math.toDegrees(-Math.atan(dirY / Math.sqrt(dirX * dirX + dirZ * dirZ)));
        return this;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}
