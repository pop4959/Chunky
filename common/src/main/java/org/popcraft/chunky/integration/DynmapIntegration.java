package org.popcraft.chunky.integration;

import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.CircleMarker;
import org.dynmap.markers.MarkerDescription;
import org.dynmap.markers.MarkerSet;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.shape.AbstractEllipse;
import org.popcraft.chunky.shape.AbstractPolygon;
import org.popcraft.chunky.shape.Shape;

import java.util.HashMap;
import java.util.Map;

public class DynmapIntegration extends AbstractMapIntegration {
    private final MarkerSet markerSet;
    private final Map<World, MarkerDescription> markers;

    public DynmapIntegration(DynmapAPI dynmapAPI) {
        this.markerSet = dynmapAPI.getMarkerAPI().createMarkerSet("chunky.markerset", this.label, null, false);
        this.markers = new HashMap<>();
    }

    @Override
    public void addShapeMarker(World world, Shape shape) {
        removeShapeMarker(world);
        if (shape instanceof AbstractPolygon) {
            AbstractPolygon polygon = (AbstractPolygon) shape;
            AreaMarker marker = markerSet.createAreaMarker(null, this.label, false, world.getName(), polygon.pointsX(), polygon.pointsZ(), false);
            marker.setLineStyle(this.weight, 1f, color);
            marker.setFillStyle(0f, 0x000000);
            markers.put(world, marker);
        } else if (shape instanceof AbstractEllipse) {
            AbstractEllipse ellipse = (AbstractEllipse) shape;
            double[] center = ellipse.getCenter();
            double[] radii = ellipse.getRadii();
            CircleMarker marker = markerSet.createCircleMarker(null, this.label, false, world.getName(), center[0], world.getSeaLevel(), center[1], radii[0], radii[1], false);
            marker.setLineStyle(this.weight, 1f, color);
            marker.setFillStyle(0f, 0x000000);
            markers.put(world, marker);
        }
    }

    @Override
    public void removeShapeMarker(World world) {
        MarkerDescription marker = markers.remove(world);
        if (marker != null) {
            marker.deleteMarker();
        }
    }

    @Override
    public void removeAllShapeMarkers() {
        if (markerSet != null) {
            markerSet.deleteMarkerSet();
        }
        markers.clear();
    }

    @Override
    public void setOptions(String label, String color, boolean hideByDefault, int priority, int weight) {
        super.setOptions(label, color, hideByDefault, priority, weight);
        if (markerSet != null) {
            markerSet.setHideByDefault(hideByDefault);
            markerSet.setLayerPriority(priority);
        }
    }
}
