package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public class Diamond extends AbstractShape {
    int p1x, p1z, p2x, p2z, p3x, p3z, p4x, p4z;

    public Diamond(Selection selection) {
        super(selection);
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
    public boolean isBounding(double x, double z) {
        if (!insideLine(p1x, p1z, p2x, p2z, x, z)) {
            return false;
        }
        if (!insideLine(p2x, p2z, p3x, p3z, x, z)) {
            return false;
        }
        if (!insideLine(p3x, p3z, p4x, p4z, x, z)) {
            return false;
        }
        return insideLine(p4x, p4z, p1x, p1z, x, z);
    }

    @Override
    public String name() {
        return "diamond";
    }
}
