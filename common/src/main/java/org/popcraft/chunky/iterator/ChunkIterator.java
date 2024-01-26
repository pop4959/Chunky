package org.popcraft.chunky.iterator;

import org.popcraft.chunky.util.ChunkCoordinate;

import java.util.Iterator;

public interface ChunkIterator extends Iterator<ChunkCoordinate> {
    long total();

    String name();

    default void postInitialization() {
    }
}
