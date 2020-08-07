package org.popcraft.chunky;

import java.util.Iterator;

public interface ChunkCoordinateIterator extends Iterator<ChunkCoordinate> {

    public ChunkCoordinate peek();

    public long count();

    public long covered();
}
