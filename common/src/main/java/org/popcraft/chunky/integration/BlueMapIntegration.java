package org.popcraft.chunky.integration;

import com.flowpowered.math.vector.Vector2d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.marker.MarkerAPI;
import de.bluecolored.bluemap.api.marker.MarkerSet;
import de.bluecolored.bluemap.api.marker.ShapeMarker;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.shape.AbstractEllipse;
import org.popcraft.chunky.shape.AbstractPolygon;
import org.popcraft.chunky.shape.Shape;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BlueMapIntegration extends AbstractMapIntegration {
    private BlueMapAPI blueMapAPI;
    private List<Runnable> pendingMarkers = new ArrayList<>();
    private static final String MARKERSET_ID = "chunky";

    public BlueMapIntegration() {
        BlueMapAPI.onEnable(blueMap -> {
            this.blueMapAPI = blueMap;
            pendingMarkers.forEach(Runnable::run);
            pendingMarkers.clear();
        });
        BlueMapAPI.onDisable(blueMap -> this.blueMapAPI = null);
    }

    @Override
    public void addShapeMarker(final World world, final Shape shape) {
        if (blueMapAPI == null) {
            this.pendingMarkers.add(() -> this.addShapeMarker(world, shape));
            return;
        }
        final MarkerAPI markerAPI;
        try {
            markerAPI = blueMapAPI.getMarkerAPI();
        } catch (IOException e) {
            return;
        }
        final MarkerSet markerSet = markerAPI.createMarkerSet(MARKERSET_ID);
        markerSet.setLabel(this.label);
        de.bluecolored.bluemap.api.marker.Shape blueShape;
        if (shape instanceof AbstractPolygon) {
            AbstractPolygon polygon = (AbstractPolygon) shape;
            double[] pointsX = polygon.pointsX();
            double[] pointsZ = polygon.pointsZ();
            if (pointsX.length != pointsZ.length) {
                return;
            }
            Vector2d[] points = new Vector2d[pointsX.length];
            for (int i = 0; i < pointsX.length; ++i) {
                points[i] = new Vector2d(pointsX[i], pointsZ[i]);
            }
            blueShape = new de.bluecolored.bluemap.api.marker.Shape(points);
        } else if (shape instanceof AbstractEllipse) {
            AbstractEllipse ellipse = (AbstractEllipse) shape;
            double[] center = ellipse.getCenter();
            double[] radii = ellipse.getRadii();
            Vector2d centerPos = new Vector2d(center[0], center[1]);
            blueShape = de.bluecolored.bluemap.api.marker.Shape.createEllipse(centerPos, radii[0], radii[1], 100);
        } else {
            return;
        }
        blueMapAPI.getWorld(world.getUUID()).ifPresent(blueWorld -> blueWorld.getMaps().forEach(map -> {
            ShapeMarker marker = markerSet.createShapeMarker(world.getName(), map, blueShape, world.getSeaLevel());
            marker.setColors(this.color, new Color(0, true));
            marker.setLabel(this.label);
        }));
        try {
            markerAPI.save();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void removeShapeMarker(final World world) {
        if (blueMapAPI == null) {
            return;
        }
        try {
            final MarkerAPI markerAPI = blueMapAPI.getMarkerAPI();
            final MarkerSet markerSet = markerAPI.createMarkerSet(MARKERSET_ID);
            blueMapAPI.getWorld(world.getUUID()).ifPresent(blueWorld -> blueWorld.getMaps().forEach(map -> {
                markerSet.removeMarker(world.getName());
            }));
            markerAPI.save();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void removeAllShapeMarkers() {
        if (blueMapAPI == null) {
            return;
        }
        try {
            final MarkerAPI markerAPI = blueMapAPI.getMarkerAPI();
            if (markerAPI.removeMarkerSet(MARKERSET_ID)) {
                markerAPI.save();
            }
        } catch (IOException ignored) {
        }
    }
}
