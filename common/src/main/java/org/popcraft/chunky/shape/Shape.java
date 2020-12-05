package org.popcraft.chunky.shape;

@FunctionalInterface
public interface Shape {
    boolean isBounding(double x, double z);

    default String name() {
        return "shape";
    }
}
