package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public class Triangle extends AbstractShape {
    private int p1x, p1z, p2x, p2z, p3x, p3z;

    public Triangle(Selection selection) {
        super(selection);
        this.p1x = radius;
        this.p1z = radius;
        this.p2x = -radius;
        this.p2z = radius;
        this.p3x = 0;
        this.p3z = -radius;
    }

    @Override
    public boolean isBounding(double x, double z) {
        if (!insideLine(p1x, p1z, p2x, p2z, x, z)) {
            return false;
        }
        if (!insideLine(p2x, p2z, p3x, p3z, x, z)) {
            return false;
        }
        return insideLine(p3x, p3z, p1x, p1z, x, z);
    }

    @Override
    public String name() {
        return "triangle";
    }
}
