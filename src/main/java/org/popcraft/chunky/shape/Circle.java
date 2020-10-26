package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public class Circle extends AbstractEllipse {
    public Circle(Selection selection, boolean chunkAligned) {
        super(selection, chunkAligned);
    }

    @Override
    public boolean isBounding(double x, double z) {
        return Math.hypot(centerX - x, centerZ - z) < radiusX;
    }

    @Override
    public String name() {
        return "circle";
    }
}
