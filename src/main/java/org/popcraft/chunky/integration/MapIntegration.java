package org.popcraft.chunky.integration;

import org.bukkit.World;
import org.popcraft.chunky.shape.Shape;

public interface MapIntegration {
    void addShapeMarker(World world, Shape shape);

    void removeShapeMarker(World world);

    void removeAllShapeMarkers();

    void setOptions(String label, String color, boolean hideByDefault, int priority, int weight);
}
