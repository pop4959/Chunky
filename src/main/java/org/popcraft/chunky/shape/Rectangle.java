package org.popcraft.chunky.shape;

public class Rectangle implements Shape {
    @Override
    public boolean isBounding(double x, double z) {
        return true;
    }

    @Override
    public String name() {
        return "rectangle";
    }
}
