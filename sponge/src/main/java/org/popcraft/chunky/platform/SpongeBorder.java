package org.popcraft.chunky.platform;

import org.popcraft.chunky.util.Coordinate;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.math.vector.Vector3d;

public class SpongeBorder implements Border {
    private WorldBorder worldBorder;

    public SpongeBorder(WorldBorder worldBorder) {
        this.worldBorder = worldBorder;
    }

    @Override
    public Coordinate getCenter() {
        Vector3d center = worldBorder.center();
        return new Coordinate(center.x(), center.z());
    }

    @Override
    public double getRadiusX() {
        return worldBorder.diameter() / 2d;
    }

    @Override
    public double getRadiusZ() {
        return worldBorder.diameter() / 2d;
    }

    @Override
    public String getShape() {
        return "square";
    }
}
