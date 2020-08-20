package org.popcraft.chunky.shape;

import org.popcraft.chunky.ChunkCoordinate;
import org.popcraft.chunky.iterator.ChunkIterator;

public abstract class AbstractShape implements Shape {
    protected ChunkCoordinate centerChunk;
    protected int diameterChunks;
    protected int diameter;
    protected int radius;
    protected int xCenter, zCenter;
    protected int x1, x2, z1, z2;

    public AbstractShape(ChunkIterator chunkIterator) {
        this.centerChunk = chunkIterator.center();
        this.diameterChunks = (int) Math.sqrt(chunkIterator.total());
        this.diameter = diameterChunks << 4;
        this.radius = diameter / 2;
        this.xCenter = (centerChunk.x << 4) + 8;
        this.zCenter = (centerChunk.z << 4) + 8;
        this.x1 = xCenter - diameter / 2;
        this.x2 = xCenter + diameter / 2;
        this.z1 = zCenter - diameter / 2;
        this.z2 = zCenter + diameter / 2;
    }

    protected boolean insideLine(double ax, double az, double bx, double bz, double cx, double cz) {
        return (bx - ax) * (cz - az) > (bz - az) * (cx - ax);
    }
}
