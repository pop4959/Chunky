package org.popcraft.chunky.platform;

import net.minecraft.world.border.WorldBorder;
import org.popcraft.chunky.util.Coordinate;

public class FabricBorder implements Border {
    private WorldBorder worldBorder;

    public FabricBorder(WorldBorder worldBorder) {
        this.worldBorder = worldBorder;
    }

    @Override
    public Coordinate getCenter() {
        return new Coordinate((int) worldBorder.getCenterX(), (int) worldBorder.getCenterZ());
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
