package org.popcraft.chunky.iterator;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.util.ChunkCoordinate;

public class ConcentricChunkIterator implements ChunkIterator {
    private final int xCenter, zCenter;
    private final int radiusChunks;
    private final long total;
    private int x, z;
    private int annulus, span;
    private int down, left, up, right;
    private boolean hasNext = true;

    public ConcentricChunkIterator(final Selection selection, final long count) {
        this(selection);
        if (count <= 0) {
            return;
        }
        int diameterFinished = (int) Math.floor(Math.sqrt(count));
        if (diameterFinished % 2 == 0) {
            --diameterFinished;
        }
        this.annulus = diameterFinished / 2 + 1;
        this.x += annulus;
        this.z += annulus;
        this.span = 2 * annulus;
        long perimeterCount = count - ((long) diameterFinished * diameterFinished) + 1;
        this.down += Math.min(span, perimeterCount);
        perimeterCount -= Math.min(span, perimeterCount);
        this.left += Math.min(span, perimeterCount);
        perimeterCount -= Math.min(span, perimeterCount);
        this.up += Math.min(span, perimeterCount);
        perimeterCount -= Math.min(span, perimeterCount);
        this.right += Math.min(span, perimeterCount);
        this.x += right - left;
        this.z += up - down;
        if (annulus > radiusChunks) {
            hasNext = false;
        }
    }

    public ConcentricChunkIterator(final Selection selection) {
        this.radiusChunks = selection.radiusChunksX();
        this.x = selection.centerChunkX();
        this.z = selection.centerChunkZ();
        this.xCenter = x;
        this.zCenter = z;
        final long diameterChunks = selection.diameterChunksX();
        this.total = diameterChunks * diameterChunks;
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public ChunkCoordinate next() {
        final ChunkCoordinate chunkCoord = new ChunkCoordinate(x, z);
        if (x == xCenter + annulus && z == zCenter + annulus) {
            ++annulus;
            ++x;
            ++z;
            if (annulus > radiusChunks) {
                hasNext = false;
            }
            span = 2 * annulus;
            down = left = up = right = 0;
        }
        if (down < span) {
            --z;
            ++down;
        } else if (left < span) {
            --x;
            ++left;
        } else if (up < span) {
            ++z;
            ++up;
        } else if (right < span) {
            ++x;
            ++right;
        }
        return chunkCoord;
    }

    @Override
    public long total() {
        return total;
    }

    @Override
    public String name() {
        return PatternType.CONCENTRIC;
    }
}
