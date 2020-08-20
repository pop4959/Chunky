package org.popcraft.chunky.shape;

import org.popcraft.chunky.ChunkCoordinate;
import org.popcraft.chunky.iterator.ChunkIterator;

public class Diamond extends AbstractShape {
    int p1x, p1z, p2x, p2z, p3x, p3z, p4x, p4z;

    public Diamond(ChunkIterator chunkIterator) {
        super(chunkIterator);
        this.p1x = xCenter;
        this.p1z = zCenter + radius;
        this.p2x = xCenter - radius;
        this.p2z = zCenter;
        this.p3x = xCenter;
        this.p3z = zCenter - radius;
        this.p4x = xCenter + radius;
        this.p4z = zCenter;
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
        if (!insideLine(p3x, p3z, p4x, p4z, xChunk, zChunk)) {
            return false;
        }
        return insideLine(p4x, p4z, p1x, p1z, xChunk, zChunk);
    }
}
