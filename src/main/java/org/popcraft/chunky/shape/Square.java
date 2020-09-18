package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public class Square extends AbstractPolygon {
    int p1x, p1z, p2x, p2z, p3x, p3z, p4x, p4z;

    protected Square(Selection selection) {
        super(selection);
        this.p1x = xCenter + radius;
        this.p1z = zCenter - radius;
        this.p2x = xCenter - radius;
        this.p2z = zCenter - radius;
        this.p3x = xCenter - radius;
        this.p3z = zCenter + radius;
        this.p4x = xCenter + radius;
        this.p4z = zCenter + radius;
    }

    @Override
    public double[] pointsX() {
        return new double[]{p1x, p2x, p3x, p4x};
    }

    @Override
    public double[] pointsZ() {
        return new double[]{p1z, p2z, p3z, p4z};
    }

    @Override
    public boolean isBounding(double x, double z) {
        return x > x1 && x < x2 && z > z1 && z < z2;
    }

    @Override
    public String name() {
        return "square";
    }
}
