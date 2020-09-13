package org.popcraft.chunky.shape;

import org.popcraft.chunky.Selection;

public class Circle extends AbstractShape {
    public Circle(Selection selection) {
        super(selection);
    }

    @Override
    public boolean isBounding(double x, double z) {
        return Math.hypot(xCenter - x, zCenter - z) < radius;
    }

    @Override
    public String name() {
        return "circle";
    }
}
