package org.popcraft.chunky.integration;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.Config;
import org.popcraft.chunky.platform.Border;
import org.popcraft.chunky.platform.util.Vector2;
import org.popcraft.chunky.shape.ShapeType;

public class WorldBorderIntegration implements BorderIntegration {
    @Override
    public boolean hasBorder(final String world) {
        return Config.Border(world) != null;
    }

    @Override
    public Border getBorder(final String world) {
        return new Border() {
            @Override
            public Vector2 getCenter() {
                final BorderData borderData = Config.Border(world);
                return Vector2.of(borderData.getX(), borderData.getZ());
            }

            @Override
            public double getRadiusX() {
                return Config.Border(world).getRadiusX();
            }

            @Override
            public double getRadiusZ() {
                return Config.Border(world).getRadiusZ();
            }

            @Override
            public String getShape() {
                final BorderData borderData = Config.Border(world);
                final double radiusX = getRadiusX();
                final double radiusZ = getRadiusZ();
                final boolean round = borderData.getShape() == null ? Config.ShapeRound() : borderData.getShape();
                final String shape;
                if (radiusX == radiusZ) {
                    shape = round ? ShapeType.CIRCLE : ShapeType.SQUARE;
                } else {
                    shape = round ? ShapeType.ELLIPSE : ShapeType.RECTANGLE;
                }
                return shape;
            }
        };
    }
}
