package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public class Circle extends AbstractEllipse {
    public Circle(final Selection selection, final boolean chunkAligned) {
        super(selection, chunkAligned);
    }

    @Override
    public boolean isBounding(final double x, final double z) {
        return Math.hypot(centerX - x, centerZ - z) <= radiusX;
    }

    @Override
    public String name() {
        return ShapeType.CIRCLE;
    }
}
