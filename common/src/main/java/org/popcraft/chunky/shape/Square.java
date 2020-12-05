package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public class Square extends AbstractPolygon {
    int p1x, p1z, p2x, p2z, p3x, p3z, p4x, p4z;

    protected Square(Selection selection, boolean chunkAligned) {
        super(selection, chunkAligned);
        this.p1x = centerX + radiusX;
        this.p1z = centerZ - radiusX;
        this.p2x = centerX - radiusX;
        this.p2z = centerZ - radiusX;
        this.p3x = centerX - radiusX;
        this.p3z = centerZ + radiusX;
        this.p4x = centerX + radiusX;
        this.p4z = centerZ + radiusX;
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
