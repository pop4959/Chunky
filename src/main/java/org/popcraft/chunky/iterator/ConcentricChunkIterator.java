package org.popcraft.chunky.iterator;

import org.popcraft.chunky.ChunkCoordinate;

public class ConcentricChunkIterator implements ChunkIterator {
    private int x, z;
    private int xCenter, zCenter;
    private int annulus, span;
    private int down, left, up, right;
    private int radiusChunks;
    private boolean hasNext = true;
    private long total;

    public ConcentricChunkIterator(int radius, int xCenter, int zCenter, long count) {
        this(radius, xCenter, zCenter);
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
        long perimeterCount = count - (diameterFinished * diameterFinished) + 1;
        this.down += Math.min(span, perimeterCount);
        perimeterCount -= Math.min(span, perimeterCount);
        this.left += Math.min(span, perimeterCount);
        perimeterCount -= Math.min(span, perimeterCount);
        this.up += Math.min(span, perimeterCount);
        perimeterCount -= Math.min(span, perimeterCount);
        this.right += Math.min(span, perimeterCount);
        this.x += right - left;
        this.z += up - down;
    }

    public ConcentricChunkIterator(int radius, int xCenter, int zCenter) {
        this.radiusChunks = (int) Math.ceil(radius / 16f);
        this.x = xCenter >> 4;
        this.z = zCenter >> 4;
        this.xCenter = x;
        this.zCenter = z;
        long diameterChunks = 2 * radiusChunks + 1;
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
        return "concentric";
    }
}
