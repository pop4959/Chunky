package org.popcraft.chunky.integration;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.Config;
import org.popcraft.chunky.platform.Border;
import org.popcraft.chunky.shape.ShapeType;
import org.popcraft.chunky.util.Coordinate;

public class WorldBorderIntegration implements BorderIntegration {
    @Override
    public boolean hasBorder(String world) {
        return Config.Border(world) != null;
    }

    @Override
    public Border getBorder(String world) {
        return new Border() {
            @Override
            public Coordinate getCenter() {
                BorderData borderData = Config.Border(world);
                return new Coordinate(borderData.getX(), borderData.getZ());
            }

            @Override
            public void setCenter(Coordinate coordinate) {
                Config.Border(world).setX(coordinate.getX());
                Config.Border(world).setZ(coordinate.getZ());
            }

            @Override
            public double getRadiusX() {
                return Config.Border(world).getRadiusX();
            }

            @Override
            public void setRadiusX(double radiusX) {
                Config.Border(world).setRadiusX((int) radiusX);
            }

            @Override
            public double getRadiusZ() {
                return Config.Border(world).getRadiusZ();
            }

            @Override
            public void setRadiusZ(double radiusZ) {
                Config.Border(world).setRadiusZ((int) radiusZ);
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
