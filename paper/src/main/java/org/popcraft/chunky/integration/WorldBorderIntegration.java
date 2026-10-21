package org.popcraft.chunky.integration;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.Config;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.popcraft.chunky.platform.Border;
import org.popcraft.chunky.platform.util.Vector2;
import org.popcraft.chunky.shape.ShapeType;
import org.popcraft.chunky.util.Reflection;

public class WorldBorderIntegration implements BorderIntegration {
    public static boolean isSupported() {
        return Reflection.classExists("com.wimbli.WorldBorder.Config")
                && Reflection.classExists("com.wimbli.WorldBorder.BorderData")
                && Reflection.methodExists(Config.class, "Border", String.class)
                && Reflection.methodExists(Config.class, "ShapeRound")
                && Reflection.methodExists(BorderData.class, "getX")
                && Reflection.methodExists(BorderData.class, "getZ")
                && Reflection.methodExists(BorderData.class, "getRadiusX")
                && Reflection.methodExists(BorderData.class, "getRadiusZ")
                && Reflection.methodExists(BorderData.class, "getShape");
    }

    @Override
    public boolean hasBorder(final String world) {
        return Config.Border(bukkitName(world)) != null;
    }

    @Override
    public Border getBorder(final String world) {
        final String bukkitWorld = bukkitName(world);
        return new Border() {
            @Override
            public Vector2 getCenter() {
                final BorderData borderData = Config.Border(bukkitWorld);
                return Vector2.of(borderData.getX(), borderData.getZ());
            }

            @Override
            public double getRadiusX() {
                return Config.Border(bukkitWorld).getRadiusX();
            }

            @Override
            public double getRadiusZ() {
                return Config.Border(bukkitWorld).getRadiusZ();
            }

            @Override
            public String getShape() {
                final BorderData borderData = Config.Border(bukkitWorld);
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

    private static String bukkitName(final String world) {
        final NamespacedKey key = NamespacedKey.fromString(world);
        final org.bukkit.World bukkitWorld = key == null ? null : Bukkit.getWorld(key);
        return bukkitWorld == null ? world : bukkitWorld.getName();
    }
}
