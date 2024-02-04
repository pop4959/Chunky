package org.popcraft.chunky.nbt.util;

public final class ChunkFilter {
    private final byte type;
    private final String name;

    private ChunkFilter(final byte type, final String name) {
        this.type = type;
        this.name = name;
    }

    public static ChunkFilter of(final byte type, final String name) {
        return new ChunkFilter(type, name);
    }

    public byte getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
