package org.popcraft.chunky.shape;

import org.popcraft.chunky.ChunkCoordinate;
import org.popcraft.chunky.iterator.ChunkIterator;

public class Triangle extends AbstractShape {
    public Triangle(ChunkIterator chunkIterator) {
        super(chunkIterator);
    }

    @Override
    public boolean isBounding(ChunkCoordinate chunkCoordinate) {
        int xChunk = (chunkCoordinate.x << 4) + 8;
        int zChunk = (chunkCoordinate.z << 4) + 8;
        int p1x = radius, p1z = radius;
        int p2x = -radius, p2z = radius;
        int p3x = 0, p3z = -radius;
        boolean insideLp1p2 = insideLine(p1x, p1z, p2x, p2z, xChunk, zChunk);
        boolean insideLp2p3 = insideLine(p2x, p2z, p3x, p3z, xChunk, zChunk);
        boolean insideLp3p1 = insideLine(p3x, p3z, p1x, p1z, xChunk, zChunk);
        return insideLp1p2 && insideLp2p3 && insideLp3p1;
    }

    private boolean insideLine(int ax, int az, int bx, int bz, int cx, int cz) {
        return (bx - ax) * (cz - az) > (bz - az) * (cx - ax);
    }
}
