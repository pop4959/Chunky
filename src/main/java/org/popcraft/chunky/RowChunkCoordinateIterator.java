package org.popcraft.chunky;

import java.util.NoSuchElementException;

public class RowChunkCoordinateIterator implements ChunkCoordinateIterator {
    private final int diameterChunks;
    private final int x1, x2, z1, z2;
    private int x, z;
    private final int recenter;
    private boolean hasNext = true;
    private ChunkCoordinate chunkCoord;
    private static final int CHUNK_SIZE = 16;

    public RowChunkCoordinateIterator(int radius, int centerX, int centerZ, long startCount) {
        this(radius, centerX, centerZ);
        this.x = x1 + ((int) (startCount / diameterChunks)) * CHUNK_SIZE;
        this.z = z1 + ((int) (startCount % diameterChunks)) * CHUNK_SIZE;
        chunkCoord = new ChunkCoordinate((x >> 4) + recenter, (z >> 4) + recenter);
    }

    public RowChunkCoordinateIterator(int radius, int centerX, int centerZ) {
        diameterChunks = radius / 8 + (radius % 8 != 0 ? 1 : 0);
        recenter = diameterChunks % 2;
        this.x1 = centerX - radius;
        this.x2 = centerX + radius;
        this.z1 = centerZ - radius;
        this.z2 = centerZ + radius;
        this.x = x1;
        this.z = z1;
        chunkCoord = new ChunkCoordinate((x >> 4) + recenter, (z >> 4) + recenter);
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public ChunkCoordinate next() {
        if (!hasNext()) throw new NoSuchElementException();
        chunkCoord = new ChunkCoordinate((x >> 4) + recenter, (z >> 4) + recenter);
        if ((z += CHUNK_SIZE) >= z2) {
            z = z1;
            if ((x += CHUNK_SIZE) >= x2) {
                hasNext = false;
            }
        }
        return chunkCoord;
    }

    @Override
    public ChunkCoordinate peek() {
        return chunkCoord;
    }

    @Override
    public long count() {
        return diameterChunks * diameterChunks;
    }
}
