package org.popcraft.chunky.platform.util;

import org.popcraft.chunky.platform.World;

public class Location {
    private World world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public Location(final World world, final double x, final double y, final double z, final float yaw, final float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Location(final World world, final double x, final double y, final double z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3 toVector() {
        return new Vector3(x, y, z);
    }

    public Location add(final Vector3 vector) {
        x += vector.getX();
        y += vector.getY();
        z += vector.getZ();
        return this;
    }

    public Location setDirection(final Vector3 direction) {
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

    public void setWorld(final World world) {
        this.world = world;
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

    public float getYaw() {
        return yaw;
    }

    public void setYaw(final float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(final float pitch) {
        this.pitch = pitch;
    }
}
