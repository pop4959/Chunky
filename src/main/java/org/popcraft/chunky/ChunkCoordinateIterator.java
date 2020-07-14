package org.popcraft.chunky;

import java.util.Iterator;

public class ChunkCoordinateIterator implements Iterator<ChunkCoordinate> {
    private final int radius;
    private int x, z;
    private boolean hasNext = true;
    private final static int CHUNK_SIZE = 16;

    public ChunkCoordinateIterator(int radius) {
        this.radius = radius;
        this.x = -radius;
        this.z = -radius;
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public ChunkCoordinate next() {
        final ChunkCoordinate chunkCoord = new ChunkCoordinate(x >> 4, z >> 4);
        if ((z += CHUNK_SIZE) > radius) {
            z = -radius;
            if ((x += CHUNK_SIZE) > radius) {
                hasNext = false;
            }
        }
        return chunkCoord;
    }

    public long count() {
        long diameterBlocks = 2 * radius;
        long diameterChunks = (long) Math.ceil((double) diameterBlocks / CHUNK_SIZE);
        return diameterChunks * diameterChunks;
    }
}
