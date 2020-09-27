package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public class Oval extends AbstractEllipse {
    protected Oval(Selection selection) {
        super(selection);
    }

    @Override
    public boolean isBounding(double x, double z) {
        return (Math.pow(x - centerX, 2) / Math.pow(radiusX, 2)) + (Math.pow(z - centerZ, 2) / Math.pow(radiusZ, 2)) <= 1;
    }

    @Override
    public String name() {
        return "oval";
    }
}
