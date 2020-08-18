package org.popcraft.chunky.iterator;

import org.popcraft.chunky.ChunkCoordinate;

public class Loop2ChunkIterator implements ChunkIterator {
    private int x, z;
    private int xCenter, zCenter;
    private int x1, x2, z1, z2;
    private int radiusChunks, diameterChunks;
    private boolean hasNext = true;
    private long total;

    public Loop2ChunkIterator(int radius, int xCenter, int zCenter, long count) {
        this(radius, xCenter, zCenter);
        this.x = x1 + (int) (count / diameterChunks);
        this.z = z1 + (int) (count % diameterChunks);
    }

    public Loop2ChunkIterator(int radius, int xCenter, int zCenter) {
        this.radiusChunks = (int) Math.ceil(radius / 16f);
        this.xCenter = xCenter >> 4;
        this.zCenter = zCenter >> 4;
        this.x1 = this.xCenter - radiusChunks;
        this.x2 = this.xCenter + radiusChunks;
        this.z1 = this.zCenter - radiusChunks;
        this.z2 = this.zCenter + radiusChunks;
        this.x = x1;
        this.z = z1;
        this.diameterChunks = 2 * radiusChunks + 1;
        this.total = diameterChunks * diameterChunks;
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public ChunkCoordinate next() {
        final ChunkCoordinate chunkCoord = new ChunkCoordinate(x, z);
        if (++z > z2) {
            z = z1;
            if (++x > x2) {
                hasNext = false;
            }
        }
        return chunkCoord;
    }

    @Override
    public ChunkCoordinate peek() {
        return new ChunkCoordinate(x, z);
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
