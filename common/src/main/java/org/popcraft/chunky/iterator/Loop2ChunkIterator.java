package org.popcraft.chunky.iterator;

import org.popcraft.chunky.util.ChunkCoordinate;
import org.popcraft.chunky.Selection;

public class Loop2ChunkIterator implements ChunkIterator {
    private int x, z;
    private int x1, x2, z1, z2;
    private long diameterChunksZ;
    private long total;
    private boolean hasNext = true;

    public Loop2ChunkIterator(Selection selection, long count) {
        this(selection);
        if (count <= 0) {
            return;
        }
        this.x = x1 + (int) (count / diameterChunksZ);
        this.z = z1 + (int) (count % diameterChunksZ);
        if (x > x2) {
            hasNext = false;
        }
    }

    public Loop2ChunkIterator(Selection selection) {
        int radiusChunksX = selection.getRadiusChunksX();
        int radiusChunksZ = selection.getRadiusChunksZ();
        int centerChunkX = selection.getChunkX();
        int centerChunkZ = selection.getChunkZ();
        this.x1 = centerChunkX - radiusChunksX;
        this.x2 = centerChunkX + radiusChunksX;
        this.z1 = centerChunkZ - radiusChunksZ;
        this.z2 = centerChunkZ + radiusChunksZ;
        this.x = x1;
        this.z = z1;
        int diameterChunksX = selection.getDiameterChunksX();
        this.diameterChunksZ = selection.getDiameterChunksZ();
        this.total = diameterChunksX * diameterChunksZ;
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
