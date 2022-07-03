package org.popcraft.chunky.util;

public final class ChunkMath {
    private ChunkMath() {
    }

    public static long pack(final int x, final int z) {
        final long lx = x & 0xFFFFFFFFL;
        final long lz = z & 0xFFFFFFFFL;
        return lx << 32 | lz;
    }

    public static int regionIndex(final int x, final int z) {
        return (x & 0x1F) * 32 + (z & 0x1F);
    }
}
