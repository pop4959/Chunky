package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public abstract class AbstractShape implements Shape {
    protected final double centerX, centerZ;
    protected final double diameterX, diameterZ;
    protected final double radiusX, radiusZ;

    protected AbstractShape(Selection selection, boolean chunkAligned) {
        if (chunkAligned) {
            this.centerX = (double) (selection.centerChunkX() << 4) + 8;
            this.centerZ = (double) (selection.centerChunkZ() << 4) + 8;
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
    }

    public double[] getCenter() {
        return new double[]{centerX, centerZ};
    }
}
