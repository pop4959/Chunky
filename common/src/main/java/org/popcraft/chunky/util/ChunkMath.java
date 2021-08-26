package org.popcraft.chunky.util;

public class ChunkMath {
    private ChunkMath() {
    }

    public static long pack(int x, int z) {
        long lx = x & 0xFFFFFFFFL;
        long lz = z & 0xFFFFFFFFL;
        return lx << 32 | lz;
    }

    public static int regionIndex(int x, int z) {
        return (x & 0x1F) * 32 + (z & 0x1F);
    }
}
