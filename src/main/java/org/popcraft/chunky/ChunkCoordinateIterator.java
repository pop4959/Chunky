package org.popcraft.chunky;

import java.util.Iterator;

public interface ChunkCoordinateIterator extends Iterator<ChunkCoordinate> {

    /**
     * Returns the last {@code ChunkCoordinate} returned by the {@code next()} method without
     * advancing the iterator.
     *
     * @return The current {@code ChunkCoordinate}.
     */
    ChunkCoordinate peek();

    /**
     * The total number of chunk coordinates that the iterator will give.
     *
     * @return the total number of chunk coordinates that the iterator will give.
     */
    long count();

}
