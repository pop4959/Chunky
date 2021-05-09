package org.popcraft.chunky.platform;

import org.popcraft.chunky.util.Coordinate;

public interface Border {
    Coordinate getCenter();

    double getRadiusX();

    double getRadiusZ();

    String getShape();
}
