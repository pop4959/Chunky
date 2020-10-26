package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public abstract class AbstractPolygon extends AbstractShape {
    public AbstractPolygon(Selection selection, boolean chunkAligned) {
        super(selection, chunkAligned);
    }

    public abstract double[] pointsX();

    public abstract double[] pointsZ();
}
