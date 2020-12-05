package org.popcraft.chunky.integration;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.Config;
import org.bukkit.Location;
import org.popcraft.chunky.platform.Border;
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
                return new Coordinate(Location.locToBlock(borderData.getX()), Location.locToBlock(borderData.getZ()));
            }

            @Override
            public int getRadiusX() {
                return Config.Border(world).getRadiusX();
            }

            @Override
            public int getRadiusZ() {
                return Config.Border(world).getRadiusZ();
            }

            @Override
            public String getShape() {
                BorderData borderData = Config.Border(world);
                int radiusX = getRadiusX();
                int radiusZ = getRadiusZ();
                boolean round = borderData.getShape() != null && borderData.getShape();
                final String shape;
                if (radiusX == radiusZ) {
                    shape = round ? "circle" : "square";
                } else {
                    shape = round ? "oval" : "rectangle";
                }
                return shape;
            }
        };
    }
}
