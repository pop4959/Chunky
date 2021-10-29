package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.util.Vector2;

public abstract class AbstractEllipse extends AbstractShape {
    protected AbstractEllipse(Selection selection, boolean chunkAligned) {
        super(selection, chunkAligned);
    }

    public Vector2 radii() {
        return Vector2.of(radiusX, radiusZ);
    }
}
