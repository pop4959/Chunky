package org.popcraft.chunky.world;

import org.popcraft.chunky.nbt.CompoundTag;

public final class Chunk {
    private CompoundTag data;
    private long lastModified;
    private int x;
    private int z;

    public Chunk(final CompoundTag data, final long lastModified, final int x, final int z) {
        this.data = data;
        this.lastModified = lastModified;
        this.x = x;
        this.z = z;
    }

    public CompoundTag getData() {
        return data;
    }

    public void setData(CompoundTag data) {
        this.data = data;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(final long lastModified) {
        this.lastModified = lastModified;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }
}
