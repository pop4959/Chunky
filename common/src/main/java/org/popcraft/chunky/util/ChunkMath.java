package org.popcraft.chunky.util;

public final class ChunkMath {
    private ChunkMath() {
    }

    /**
     * Packs two chunk coordinates into a single {@code long} for zero-allocation storage.
     * Upper 32 bits = x, lower 32 bits = z (both treated as unsigned to preserve negatives).
     */
    public static long pack(final int x, final int z) {
        final long lx = x & 0xFFFFFFFFL;
        final long lz = z & 0xFFFFFFFFL;
        return lx << 32 | lz;
    }

    /** Extracts the chunk X coordinate from a value packed with {@link #pack}. */
    public static int unpackX(final long packed) {
        return (int) (packed >>> 32);
    }

    /** Extracts the chunk Z coordinate from a value packed with {@link #pack}. */
    public static int unpackZ(final long packed) {
        return (int) (packed & 0xFFFFFFFFL);
    }

    public static int regionIndex(final int x, final int z) {
        return (x & 0x1F) * 32 + (z & 0x1F);
    }
}
