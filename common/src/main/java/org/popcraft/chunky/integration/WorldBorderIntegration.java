package org.popcraft.chunky.integration;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.Config;
import org.popcraft.chunky.platform.Border;
import org.popcraft.chunky.platform.util.Vector2;
import org.popcraft.chunky.shape.ShapeType;

public class WorldBorderIntegration implements BorderIntegration {
    @Override
    public boolean hasBorder(String world) {
        return Config.Border(world) != null;
    }

    @Override
    public Border getBorder(String world) {
        return new Border() {
            @Override
            public Vector2 getCenter() {
                BorderData borderData = Config.Border(world);
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
                BorderData borderData = Config.Border(world);
                double radiusX = getRadiusX();
                double radiusZ = getRadiusZ();
                boolean round = borderData.getShape() == null ? Config.ShapeRound() : borderData.getShape();
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
