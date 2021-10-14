package org.popcraft.chunky.platform;

import net.minecraft.world.border.WorldBorder;
import org.popcraft.chunky.shape.ShapeType;
import org.popcraft.chunky.util.Coordinate;

public class FabricBorder implements Border {
    private final WorldBorder worldBorder;

    public FabricBorder(WorldBorder worldBorder) {
        this.worldBorder = worldBorder;
    }

    @Override
    public Coordinate getCenter() {
        return new Coordinate(worldBorder.getCenterX(), worldBorder.getCenterZ());
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
