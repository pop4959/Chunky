package org.popcraft.chunky.iterator;

import org.popcraft.chunky.ChunkCoordinate;
import org.popcraft.chunky.Selection;

public class Loop2ChunkIterator implements ChunkIterator {
    private int x, z;
    private int x1, x2, z1, z2;
    private long diameterChunksZ;
    private boolean hasNext = true;
    private long total;

    public Loop2ChunkIterator(Selection selection, long count) {
        this(selection);
        if (count <= 0) {
            return;
        }
        this.x = x1 + (int) (count / diameterChunksZ);
        this.z = z1 + (int) (count % diameterChunksZ);
    }

    public Loop2ChunkIterator(Selection selection) {
        int radiusChunks = selection.getRadiusChunks();
        int radiusChunksZ = selection.getRadiusChunksZ();
        int xCenterChunk = selection.getChunkX();
        int zCenterChunk = selection.getChunkZ();
        this.x1 = xCenterChunk - radiusChunks;
        this.x2 = xCenterChunk + radiusChunks;
        this.z1 = zCenterChunk - radiusChunksZ;
        this.z2 = zCenterChunk + radiusChunksZ;
        this.x = x1;
        this.z = z1;
        int diameterChunks = selection.getDiameterChunks();
        this.diameterChunksZ = selection.getDiameterChunksZ();
        this.total = diameterChunks * diameterChunksZ;
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
    public long total() {
        return total;
    }

    @Override
    public String name() {
        return "loop";
    }
}
