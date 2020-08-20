package org.popcraft.chunky.shape;

import org.popcraft.chunky.ChunkCoordinate;
import org.popcraft.chunky.iterator.ChunkIterator;

public class Triangle extends AbstractShape {
    private int p1x, p1z, p2x, p2z, p3x, p3z;

    public Triangle(ChunkIterator chunkIterator) {
        super(chunkIterator);
        this.p1x = radius;
        this.p1z = radius;
        this.p2x = -radius;
        this.p2z = radius;
        this.p3x = 0;
        this.p3z = -radius;
    }

    @Override
    public boolean isBounding(ChunkCoordinate chunkCoordinate) {
        int xChunk = (chunkCoordinate.x << 4) + 8;
        int zChunk = (chunkCoordinate.z << 4) + 8;
        if (!insideLine(p1x, p1z, p2x, p2z, xChunk, zChunk)) {
            return false;
        }
        if (!insideLine(p2x, p2z, p3x, p3z, xChunk, zChunk)) {
            return false;
        }
        return insideLine(p3x, p3z, p1x, p1z, xChunk, zChunk);
    }
}
