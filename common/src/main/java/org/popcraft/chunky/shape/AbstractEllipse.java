package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public abstract class AbstractEllipse extends AbstractShape {
    protected AbstractEllipse(Selection selection, boolean chunkAligned) {
        super(selection, chunkAligned);
    }

    public double[] getRadii() {
        return new double[]{radiusX, radiusZ};
    }
}
