package org.popcraft.chunky.platform;

import org.popcraft.chunky.platform.util.Vector2;

public interface Border {
    Vector2 getCenter();

    double getRadiusX();

    double getRadiusZ();

    String getShape();
}
