package org.popcraft.chunky.platform;

import com.flowpowered.math.vector.Vector3d;
import org.popcraft.chunky.util.Coordinate;
import org.spongepowered.api.world.WorldBorder;

public class SpongeBorder implements Border {
    private WorldBorder worldBorder;

    public SpongeBorder(WorldBorder worldBorder) {
        this.worldBorder = worldBorder;
    }

    @Override
    public Coordinate getCenter() {
        Vector3d center = worldBorder.getCenter();
        return new Coordinate(center.getX(), center.getZ());
    }

    @Override
    public double getRadiusX() {
        return worldBorder.getDiameter() / 2d;
    }

    @Override
    public double getRadiusZ() {
        return worldBorder.getDiameter() / 2d;
    }

    @Override
    public String getShape() {
        return "square";
    }
}
