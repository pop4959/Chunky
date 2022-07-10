package org.popcraft.chunky.iterator;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.util.ChunkCoordinate;

import java.util.NoSuchElementException;

public class Loop2ChunkIterator implements ChunkIterator {
    private final int x1, x2, z1, z2;
    private final long diameterChunksZ;
    private final long total;
    private int x, z;
    private boolean hasNext = true;

    public Loop2ChunkIterator(final Selection selection, final long count) {
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

    public Loop2ChunkIterator(final Selection selection) {
        final int radiusChunksX = selection.radiusChunksX();
        final int radiusChunksZ = selection.radiusChunksZ();
        final int centerChunkX = selection.centerChunkX();
        final int centerChunkZ = selection.centerChunkZ();
        this.x1 = centerChunkX - radiusChunksX;
        this.x2 = centerChunkX + radiusChunksX;
        this.z1 = centerChunkZ - radiusChunksZ;
        this.z2 = centerChunkZ + radiusChunksZ;
        this.x = x1;
        this.z = z1;
        final int diameterChunksX = selection.diameterChunksX();
        this.diameterChunksZ = selection.diameterChunksZ();
        this.total = diameterChunksX * diameterChunksZ;
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public ChunkCoordinate next() {
        if (!hasNext) {
            throw new NoSuchElementException();
        }
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
        return PatternType.LOOP;
    }
}
