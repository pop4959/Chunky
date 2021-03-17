package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public abstract class AbstractShape implements Shape {
    protected int centerX, centerZ;
    protected int diameterX, diameterZ;
    protected int radiusX, radiusZ;
    protected int x1, x2, z1, z2;

    protected AbstractShape(Selection selection, boolean chunkAligned) {
        if (chunkAligned) {
            this.centerX = (selection.centerChunkX() << 4) + 8;
            this.centerZ = (selection.centerChunkZ() << 4) + 8;
            this.diameterX = selection.diameterChunksX() << 4;
            this.diameterZ = selection.diameterChunksZ() << 4;
            this.radiusX = diameterX / 2;
            this.radiusZ = diameterZ / 2;
        } else {
            this.centerX = selection.centerX();
            this.centerZ = selection.centerZ();
            this.radiusX = selection.radiusX();
            this.radiusZ = selection.radiusZ();
            this.diameterX = 2 * radiusX;
            this.diameterZ = 2 * radiusZ;
        }
        this.x1 = centerX - radiusX;
        this.x2 = centerX + radiusX;
        this.z1 = centerZ - radiusZ;
        this.z2 = centerZ + radiusZ;
    }

    public double[] getCenter() {
        return new double[]{centerX, centerZ};
    }
}
