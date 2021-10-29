package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.util.Vector2;

import java.util.Arrays;
import java.util.List;

public class Rectangle extends AbstractPolygon {
    final double b1x, b1z, b2x, b2z;
    final double p1x, p1z, p2x, p2z, p3x, p3z, p4x, p4z;

    protected Rectangle(Selection selection, boolean chunkAligned) {
        super(selection, chunkAligned);
        this.b1x = centerX - radiusX;
        this.b1z = centerZ - radiusZ;
        this.b2x = centerX + radiusX;
        this.b2z = centerZ + radiusZ;
        this.p1x = centerX + radiusX;
        this.p1z = centerZ - radiusZ;
        this.p2x = centerX - radiusX;
        this.p2z = centerZ - radiusZ;
        this.p3x = centerX - radiusX;
        this.p3z = centerZ + radiusZ;
        this.p4x = centerX + radiusX;
        this.p4z = centerZ + radiusZ;
    }

    @Override
    public List<Vector2> points() {
        return Arrays.asList(
                Vector2.of(p1x, p1z),
                Vector2.of(p2x, p2z),
                Vector2.of(p3x, p3z),
                Vector2.of(p4x, p4z)
        );
    }

    @Override
    public boolean isBounding(double x, double z) {
        return x >= b1x && x <= b2x && z >= b1z && z <= b2z;
    }

    @Override
    public String name() {
        return ShapeType.RECTANGLE;
    }
}
