package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public class Star extends AbstractShape {
    private double p1x, p1z, p2x, p2z, p3x, p3z, p4x, p4z, p5x, p5z;

    public Star(Selection selection) {
        super(selection);
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
    public boolean isBounding(double x, double z) {
        boolean inside13 = insideLine(p1x, p1z, p3x, p3z, x, z);
        boolean inside24 = insideLine(p2x, p2z, p4x, p4z, x, z);
        boolean inside35 = insideLine(p3x, p3z, p5x, p5z, x, z);
        boolean inside41 = insideLine(p4x, p4z, p1x, p1z, x, z);
        if (inside13 && inside24 && inside35 && inside41) {
            return true;
        }
        boolean inside52 = insideLine(p5x, p5z, p2x, p2z, x, z);
        if (inside24 && inside35 && inside41 && inside52) {
            return true;
        }
        if (inside35 && inside41 && inside52 && inside13) {
            return true;
        }
        if (inside41 && inside52 && inside13 && inside24) {
            return true;
        }
        return inside52 && inside13 && inside24 && inside35;
    }

    @Override
    public String name() {
        return "star";
    }
}
