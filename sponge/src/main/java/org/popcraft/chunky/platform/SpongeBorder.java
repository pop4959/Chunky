package org.popcraft.chunky.platform;

import org.popcraft.chunky.util.Coordinate;
import org.spongepowered.api.world.border.WorldBorder;
import org.spongepowered.math.vector.Vector2d;

public class SpongeBorder implements Border {
    private final WorldBorder worldBorder;

    public SpongeBorder(WorldBorder worldBorder) {
        this.worldBorder = worldBorder;
    }

    @Override
    public Coordinate getCenter() {
        Vector2d center = worldBorder.center();
        return new Coordinate(center.x(), center.y());
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
