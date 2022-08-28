package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.util.Vector2;

public class Ellipse extends AbstractEllipse {
    public Ellipse(final Selection selection, final boolean chunkAligned) {
        super(selection, chunkAligned);
    }

    @Override
    public boolean isBounding(final double x, final double z) {
        return (Math.pow(x - centerX, 2) / Math.pow(radiusX, 2)) + (Math.pow(z - centerZ, 2) / Math.pow(radiusZ, 2)) <= 1;
    }

    @Override
    public String name() {
        return ShapeType.ELLIPSE;
    }

    @Override
    public Vector2 radii() {
        return Vector2.of(radiusX, radiusZ);
    }
}
