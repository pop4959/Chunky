package org.popcraft.chunky.integration;

import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.CircleMarker;
import org.dynmap.markers.MarkerDescription;
import org.dynmap.markers.MarkerSet;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.platform.util.Vector2;
import org.popcraft.chunky.shape.AbstractEllipse;
import org.popcraft.chunky.shape.AbstractPolygon;
import org.popcraft.chunky.shape.Shape;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynmapIntegration extends AbstractMapIntegration {
    private final MarkerSet markerSet;
    private final Map<String, MarkerDescription> markers;

    public DynmapIntegration(DynmapAPI dynmapAPI) {
        this.markerSet = dynmapAPI.getMarkerAPI().createMarkerSet("chunky.markerset", this.label, null, false);
        this.markers = new HashMap<>();
    }

    @Override
    public void addShapeMarker(World world, Shape shape) {
        removeShapeMarker(world);
        if (shape instanceof AbstractPolygon) {
            final AbstractPolygon polygon = (AbstractPolygon) shape;
            final List<Vector2> points = polygon.points();
            final int size = points.size();
            final double[] pointsX = new double[size];
            final double[] pointsZ = new double[size];
            for (int i = 0; i < size; ++i) {
                final Vector2 point = points.get(i);
                pointsX[i] = point.getX();
                pointsZ[i] = point.getZ();
            }
            final AreaMarker marker = markerSet.createAreaMarker(null, this.label, false, world.getName(), pointsX, pointsZ, false);
            marker.setLineStyle(this.weight, 1f, color);
            marker.setFillStyle(0f, 0x000000);
            markers.put(world.getName(), marker);
        } else if (shape instanceof AbstractEllipse) {
            final AbstractEllipse ellipse = (AbstractEllipse) shape;
            final Vector2 center = ellipse.center();
            final Vector2 radii = ellipse.radii();
            final CircleMarker marker = markerSet.createCircleMarker(null, this.label, false, world.getName(), center.getX(), world.getSeaLevel(), center.getZ(), radii.getX(), radii.getZ(), false);
            marker.setLineStyle(this.weight, 1f, color);
            marker.setFillStyle(0f, 0x000000);
            markers.put(world.getName(), marker);
        }
    }

    @Override
    public void removeShapeMarker(World world) {
        MarkerDescription marker = markers.remove(world.getName());
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
