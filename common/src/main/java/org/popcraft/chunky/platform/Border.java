package org.popcraft.chunky.platform;

import org.popcraft.chunky.util.Coordinate;

public interface Border {
    Coordinate getCenter();

    void setCenter(Coordinate coordinate);

    double getRadiusX();

    void setRadiusX(double radiusX);

    double getRadiusZ();

    void setRadiusZ(double radiusZ);

    String getShape();
}
