package org.popcraft.chunky.iterator;

import org.popcraft.chunky.ChunkCoordinate;

public class LoopChunkIterator implements ChunkIterator {
    private int x, z;
    private int xCenter, zCenter;
    private int x1, x2, z1, z2;
    private int diameterChunks;
    private boolean hasNext = true;
    private long total;
    private static final int CHUNK_SIZE = 16;

    public LoopChunkIterator(int radius, int xCenter, int zCenter, long count) {
        this(radius, xCenter, zCenter);
        this.x = x1 + ((int) (count / diameterChunks)) * CHUNK_SIZE;
        this.z = z1 + ((int) (count % diameterChunks)) * CHUNK_SIZE;
    }

    public LoopChunkIterator(int radius, int xCenter, int zCenter) {
        int diameter = 2 * radius;
        if (diameter % CHUNK_SIZE != 0) {
            ++diameterChunks;
        }
        this.diameterChunks += diameter / CHUNK_SIZE;
        this.total = diameterChunks * diameterChunks;
        this.x1 = xCenter - radius;
        this.x2 = xCenter + radius;
        this.z1 = zCenter - radius;
        this.z2 = zCenter + radius;
        this.x = x1;
        this.z = z1;
        this.xCenter = xCenter >> 4;
        this.zCenter = zCenter >> 4;
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

    @Override
    public ChunkCoordinate peek() {
        return new ChunkCoordinate(x >> 4, z >> 4);
    }

    @Override
    public ChunkCoordinate center() {
        return new ChunkCoordinate(xCenter, zCenter);
    }

    @Override
    public long total() {
        return total;
    }

    @Override
    public String name() {
        return "loop";
    }
}
