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
        return new Coordinate(center.getFloorX(), center.getFloorZ());
    }

    @Override
    public int getRadiusX() {
        return (int) worldBorder.getDiameter() / 2;
    }

    @Override
    public int getRadiusZ() {
        return (int) worldBorder.getDiameter() / 2;
    }

    @Override
    public String getShape() {
        return "square";
    }
}
