package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public abstract class AbstractEllipse extends AbstractShape {
    public AbstractEllipse(Selection selection) {
        super(selection);
    }

    public double[] getRadii() {
        return new double[]{radiusX, radiusZ};
    }
}
