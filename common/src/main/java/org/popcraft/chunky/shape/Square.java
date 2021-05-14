package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public class Square extends AbstractPolygon {
    double b1x, b1z, b2x, b2z;
    double p1x, p1z, p2x, p2z, p3x, p3z, p4x, p4z;

    protected Square(Selection selection, boolean chunkAligned) {
        super(selection, chunkAligned);
        this.b1x = centerX - radiusX;
        this.b1z = centerZ - radiusX;
        this.b2x = centerX + radiusX;
        this.b2z = centerZ + radiusX;
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
        return x >= b1x && x <= b2x && z >= b1z && z <= b2z;
    }

    @Override
    public String name() {
        return "square";
    }
}
