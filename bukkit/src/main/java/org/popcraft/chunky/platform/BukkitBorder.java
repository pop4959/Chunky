package org.popcraft.chunky.platform;

import org.bukkit.WorldBorder;
import org.popcraft.chunky.util.Coordinate;

public class BukkitBorder implements Border {
    WorldBorder worldBorder;

    public BukkitBorder(WorldBorder worldBorder) {
        this.worldBorder = worldBorder;
    }

    @Override
    public Coordinate getCenter() {
        return new Coordinate(worldBorder.getCenter().getX(), worldBorder.getCenter().getZ());
    }

    @Override
    public double getRadiusX() {
        return worldBorder.getSize() / 2d;
    }

    @Override
    public double getRadiusZ() {
        return worldBorder.getSize() / 2d;
    }

    @Override
    public String getShape() {
        return "square";
    }
}
