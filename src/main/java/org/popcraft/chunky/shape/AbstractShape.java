package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public abstract class AbstractShape implements Shape {
    protected int xCenter, zCenter;
    protected int diameter, diameterZ;
    protected int radius, radiusZ;
    protected int x1, x2, z1, z2;

    protected AbstractShape(Selection selection) {
        this.xCenter = (selection.getChunkX() << 4) + 8;
        this.zCenter = (selection.getChunkZ() << 4) + 8;
        this.diameter = selection.getDiameterChunks() << 4;
        this.diameterZ = selection.getDiameterChunksZ() << 4;
        this.radius = diameter / 2;
        this.radiusZ = diameterZ / 2;
        this.x1 = xCenter - radius;
        this.x2 = xCenter + radius;
        this.z1 = zCenter - radiusZ;
        this.z2 = zCenter + radiusZ;
    }

    public double[] getCenter() {
        return new double[]{xCenter, zCenter};
    }
}
