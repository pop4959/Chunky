package org.popcraft.chunky.nbt.util;

import org.popcraft.chunky.nbt.Tag;

public final class Chunk {
    private int x;
    private int z;
    private Tag data;
    private long lastModified;

    public Chunk(final int x, final int z, final Tag data, final long lastModified) {
        this.x = x;
        this.z = z;
        this.data = data;
        this.lastModified = lastModified;
    }

    public Tag getData() {
        return data;
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

    public void setData(Tag data) {
        this.data = data;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(final long lastModified) {
        this.lastModified = lastModified;
    }
}
