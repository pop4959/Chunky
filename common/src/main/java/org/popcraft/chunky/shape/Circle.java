package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.util.Vector2;

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

    @Override
    public Vector2 radii() {
        return Vector2.of(radiusX, radiusX);
    }
}
