package org.popcraft.chunky.shape;

import org.popcraft.chunky.ChunkCoordinate;
import org.popcraft.chunky.iterator.ChunkIterator;

public class Pentagon extends AbstractShape {
    private double p1x, p1z, p2x, p2z, p3x, p3z, p4x, p4z, p5x, p5z;

    public Pentagon(ChunkIterator chunkIterator) {
        super(chunkIterator);
        this.p1x = xCenter + radius * Math.cos(Math.toRadians(54));
        this.p1z = zCenter + radius * Math.sin(Math.toRadians(54));
        this.p2x = xCenter + radius * Math.cos(Math.toRadians(126));
        this.p2z = zCenter + radius * Math.sin(Math.toRadians(126));
        this.p3x = xCenter + radius * Math.cos(Math.toRadians(198));
        this.p3z = zCenter + radius * Math.sin(Math.toRadians(198));
        this.p4x = xCenter + radius * Math.cos(Math.toRadians(270));
        this.p4z = zCenter + radius * Math.sin(Math.toRadians(270));
        this.p5x = xCenter + radius * Math.cos(Math.toRadians(342));
        this.p5z = zCenter + radius * Math.sin(Math.toRadians(342));
    }

    @Override
    public boolean isBounding(ChunkCoordinate chunkCoordinate) {
        int xChunk = (chunkCoordinate.x << 4) + 8;
        int zChunk = (chunkCoordinate.z << 4) + 8;
        boolean inside12 = insideLine(p1x, p1z, p2x, p2z, xChunk, zChunk);
        boolean inside23 = insideLine(p2x, p2z, p3x, p3z, xChunk, zChunk);
        boolean inside34 = insideLine(p3x, p3z, p4x, p4z, xChunk, zChunk);
        boolean inside45 = insideLine(p4x, p4z, p5x, p5z, xChunk, zChunk);
        boolean inside51 = insideLine(p5x, p5z, p1x, p1z, xChunk, zChunk);
        return inside12 && inside23 && inside34 && inside45 && inside51;
    }
}
