package org.popcraft.chunky.iterator;

import org.popcraft.chunky.util.ChunkCoordinate;
import org.popcraft.chunky.util.ChunkMath;

import java.util.Iterator;

public interface ChunkIterator extends Iterator<ChunkCoordinate> {
    long total();

    String name();

    default boolean process() {
        return true;
    }

    /**
     * Returns the next chunk position as a primitive {@code long} packed via {@link ChunkMath#pack}.
     * <p>
     * This is the zero-allocation path for the dispatch hot-loop: no {@link ChunkCoordinate} record
     * is instantiated. Concrete iterators should override this to avoid calling {@link #next()} at all.
     * Use {@link ChunkMath#unpackX} / {@link ChunkMath#unpackZ} to extract coordinates.
     *
     * @return packed {@code long} where upper 32 bits = chunkX, lower 32 bits = chunkZ
     */
    default long nextLong() {
        final ChunkCoordinate c = next();
        return ChunkMath.pack(c.x(), c.z());
    }
}
