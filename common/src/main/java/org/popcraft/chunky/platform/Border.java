package org.popcraft.chunky.platform;

import org.popcraft.chunky.util.Coordinate;

public interface Border {
    Coordinate getCenter();

    int getRadiusX();

    int getRadiusZ();

    String getShape();
}
