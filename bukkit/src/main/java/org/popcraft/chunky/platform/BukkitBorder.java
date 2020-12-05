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
        return new Coordinate(worldBorder.getCenter().getBlockX(), worldBorder.getCenter().getBlockZ());
    }

    @Override
    public int getRadiusX() {
        return (int) worldBorder.getSize() / 2;
    }

    @Override
    public int getRadiusZ() {
        return (int) worldBorder.getSize() / 2;
    }

    @Override
    public String getShape() {
        return "square";
    }
}
