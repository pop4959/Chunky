package org.popcraft.chunky.integration;

import org.bukkit.World;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.CircleMarker;
import org.dynmap.markers.MarkerDescription;
import org.dynmap.markers.MarkerSet;
import org.popcraft.chunky.shape.AbstractEllipse;
import org.popcraft.chunky.shape.AbstractPolygon;
import org.popcraft.chunky.shape.Shape;

import java.util.HashMap;
import java.util.Map;

public class DynmapIntegration implements MapIntegration {
    private MarkerSet markerSet;
    private Map<String, MarkerDescription> markers;
    private final String SET_LABEL = "chunky.markerset";
    private final String MARKER_LABEL = "World Border";

    public DynmapIntegration(DynmapAPI dynmapAPI) {
        this.markerSet = dynmapAPI.getMarkerAPI().createMarkerSet(SET_LABEL, MARKER_LABEL, null, false);
        this.markers = new HashMap<>();
    }

    @Override
    public void addShapeMarker(World world, Shape shape) {
        removeShapeMarker(world);
        String label = shapeLabel(world);
        if (shape instanceof AbstractPolygon) {
            AbstractPolygon polygon = (AbstractPolygon) shape;
            AreaMarker marker = markerSet.createAreaMarker(label, MARKER_LABEL, false, world.getName(), polygon.pointsX(), polygon.pointsZ(), false);
            marker.setLineStyle(3, 1f, 0xFF0000);
            marker.setFillStyle(0f, 0x000000);
            markers.put(label, marker);
        } else if (shape instanceof AbstractEllipse) {
            AbstractEllipse ellipse = (AbstractEllipse) shape;
            double[] center = ellipse.getCenter();
            double[] radii = ellipse.getRadii();
            CircleMarker marker = markerSet.createCircleMarker(label, MARKER_LABEL, false, world.getName(), center[0], world.getSeaLevel(), center[1], radii[0], radii[1], false);
            marker.setLineStyle(3, 1f, 0xFF0000);
            marker.setFillStyle(0f, 0x000000);
            markers.put(label, marker);
        }
    }

    @Override
    public void removeShapeMarker(World world) {
        MarkerDescription marker = markers.remove(shapeLabel(world));
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

    private String shapeLabel(World world) {
        return String.format("%s.%s", SET_LABEL, world.getName());
    }
}
