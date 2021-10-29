package org.popcraft.chunky.platform;

import org.bukkit.WorldBorder;
import org.popcraft.chunky.platform.util.Vector2;
import org.popcraft.chunky.shape.ShapeType;

public class BukkitBorder implements Border {
    final WorldBorder worldBorder;

    public BukkitBorder(WorldBorder worldBorder) {
        this.worldBorder = worldBorder;
    }

    @Override
    public Vector2 getCenter() {
        return Vector2.of(worldBorder.getCenter().getX(), worldBorder.getCenter().getZ());
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
        return ShapeType.SQUARE;
    }
}
