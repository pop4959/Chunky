package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public abstract class AbstractShape implements Shape {
    protected int xCenter, zCenter;
    protected int diameter;
    protected int radius;
    protected int x1, x2, z1, z2;

    protected AbstractShape(Selection selection) {
        this.xCenter = (selection.getChunkX() << 4) + 8;
        this.zCenter = (selection.getChunkZ() << 4) + 8;
        this.diameter = selection.getDiameterChunks() << 4;
        this.radius = diameter / 2;
        this.x1 = xCenter - diameter / 2;
        this.x2 = xCenter + diameter / 2;
        this.z1 = zCenter - diameter / 2;
        this.z2 = zCenter + diameter / 2;
    }

    protected boolean insideLine(double ax, double az, double bx, double bz, double cx, double cz) {
        return (bx - ax) * (cz - az) > (bz - az) * (cx - ax);
    }
}
