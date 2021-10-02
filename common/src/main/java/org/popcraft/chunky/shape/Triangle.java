package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

import static org.popcraft.chunky.shape.ShapeUtil.insideLine;

public class Triangle extends AbstractPolygon {
    private final double p1x, p1z, p2x, p2z, p3x, p3z;

    public Triangle(Selection selection, boolean chunkAligned) {
        super(selection, chunkAligned);
        this.p1x = centerX + radiusX;
        this.p1z = centerZ + radiusX;
        this.p2x = centerX - radiusX;
        this.p2z = centerZ + radiusX;
        this.p3x = centerX;
        this.p3z = centerZ - radiusX;
    }

    @Override
    public double[] pointsX() {
        return new double[]{p1x, p2x, p3x};
    }

    @Override
    public double[] pointsZ() {
        return new double[]{p1z, p2z, p3z};
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
        return ShapeType.TRIANGLE;
    }
}
