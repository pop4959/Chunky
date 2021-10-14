package org.popcraft.chunky.platform;

import org.bukkit.WorldBorder;
import org.popcraft.chunky.shape.ShapeType;
import org.popcraft.chunky.util.Coordinate;

public class BukkitBorder implements Border {
    final WorldBorder worldBorder;

    public BukkitBorder(WorldBorder worldBorder) {
        this.worldBorder = worldBorder;
    }

    @Override
    public Coordinate getCenter() {
        return new Coordinate(worldBorder.getCenter().getX(), worldBorder.getCenter().getZ());
    }

    @Override
    public void setCenter(Coordinate coordinate) {
        worldBorder.setCenter(coordinate.getX(), coordinate.getZ());
    }

    @Override
    public double getRadiusX() {
        return worldBorder.getSize() / 2d;
    }

    @Override
    public void setRadiusX(double radiusX) {
        worldBorder.setSize(radiusX * 2);
    }

    @Override
    public double getRadiusZ() {
        return worldBorder.getSize() / 2d;
    }

    @Override
    public void setRadiusZ(double radiusZ) {
        worldBorder.setSize(radiusZ * 2);
    }

    @Override
    public String getShape() {
        return ShapeType.SQUARE;
    }
}
