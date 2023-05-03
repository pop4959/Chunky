package org.popcraft.chunky.nbt.util;

import org.popcraft.chunky.nbt.CompoundTag;

public final class Chunk {
    private CompoundTag data;
    private long lastModified;

    public Chunk(final CompoundTag data, final long lastModified) {
        this.data = data;
        this.lastModified = lastModified;
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
}
