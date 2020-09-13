package org.popcraft.chunky.iterator;

import org.popcraft.chunky.ChunkCoordinate;

import java.util.Iterator;

public interface ChunkIterator extends Iterator<ChunkCoordinate> {
    long total();

    String name();
}
