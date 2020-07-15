package org.popcraft.chunky;

import java.util.Iterator;

public class ChunkCoordinateIterator implements Iterator<ChunkCoordinate> {
    private final int radius;
    private int x, z;
    private int x1, x2, z1, z2;
    private boolean hasNext = true;
    private final static int CHUNK_SIZE = 16;

    public ChunkCoordinateIterator(int radius, int centerX, int centerZ) {
        this.radius = radius;
        this.x1 = centerX - radius;
        this.x2 = centerX + radius;
        this.z1 = centerZ - radius;
        this.z2 = centerZ + radius;
        this.x = x1;
        this.z = z1;
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public ChunkCoordinate next() {
        final ChunkCoordinate chunkCoord = new ChunkCoordinate(x >> 4, z >> 4);
        if ((z += CHUNK_SIZE) >= z2) {
            z = z1;
            if ((x += CHUNK_SIZE) >= x2) {
                hasNext = false;
            }
        }
        return chunkCoord;
    }

    public ChunkCoordinate peek() {
        return new ChunkCoordinate(x >> 4, z >> 4);
    }

    public long count() {
        long diameterBlocks = 2 * radius;
        long diameterChunks = 0;
        if (diameterBlocks % CHUNK_SIZE != 0) {
            ++diameterChunks;
        }
        diameterChunks += diameterBlocks / CHUNK_SIZE;
        return diameterChunks * diameterChunks;
    }
}
