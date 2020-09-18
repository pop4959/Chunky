package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public class Oval extends AbstractShape {
    protected Oval(Selection selection) {
        super(selection);
    }

    @Override
    public boolean isBounding(double x, double z) {
        return (Math.pow(x - xCenter, 2) / Math.pow(radius, 2)) + (Math.pow(z - zCenter, 2) / Math.pow(radiusZ, 2)) <= 1;
    }

    @Override
    public String name() {
        return "oval";
    }
}
