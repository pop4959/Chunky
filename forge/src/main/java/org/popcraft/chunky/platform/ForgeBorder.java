package org.popcraft.chunky.platform;

import net.minecraft.world.level.border.WorldBorder;
import org.popcraft.chunky.platform.util.Vector2;
import org.popcraft.chunky.shape.ShapeType;

public class ForgeBorder implements Border {
    private final WorldBorder worldBorder;

    public ForgeBorder(WorldBorder worldBorder) {
        this.worldBorder = worldBorder;
    }

    @Override
    public Vector2 getCenter() {
        return Vector2.of(worldBorder.getCenterX(), worldBorder.getCenterZ());
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
