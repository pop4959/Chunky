package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public abstract class AbstractPolygon extends AbstractShape {
    protected AbstractPolygon(Selection selection) {
        super(selection);
    }

    public abstract double[] pointsX();

    public abstract double[] pointsZ();
}
