package org.popcraft.chunky.platform;

import net.minecraft.world.level.border.WorldBorder;
import org.popcraft.chunky.util.Coordinate;

public class ForgeBorder implements Border {
    private final WorldBorder worldBorder;

    public ForgeBorder(WorldBorder worldBorder) {
        this.worldBorder = worldBorder;
    }

    @Override
    public Coordinate getCenter() {
        return new Coordinate(worldBorder.getCenterX(), worldBorder.getCenterZ());
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
