package org.popcraft.chunky.integration;

import net.pl3x.map.api.Key;
import net.pl3x.map.api.LayerProvider;
import net.pl3x.map.api.Pl3xMap;
import net.pl3x.map.api.Point;
import net.pl3x.map.api.Registry;
import net.pl3x.map.api.SimpleLayerProvider;
import net.pl3x.map.api.marker.Marker;
import net.pl3x.map.api.marker.MarkerOptions;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.shape.AbstractEllipse;
import org.popcraft.chunky.shape.AbstractPolygon;
import org.popcraft.chunky.shape.Circle;
import org.popcraft.chunky.shape.Shape;

public class Pl3xMapIntegration extends AbstractMapIntegration {
    private Pl3xMap pl3xMap;
    private boolean hideByDefault;
    private int priority;
    private int weight = 3;
    private final static Key CHUNKY_KEY = Key.of("chunky");

    public Pl3xMapIntegration(Pl3xMap pl3xMap) {
        this.pl3xMap = pl3xMap;
    }

    @Override
    public void addShapeMarker(final World world, final Shape shape) {
        pl3xMap.mapWorlds().stream()
                .filter(mapWorld -> mapWorld.uuid().equals(world.getUUID()))
                .findFirst()
                .ifPresent(pl3xWorld -> {
                    Registry<LayerProvider> layerRegistry = pl3xWorld.layerRegistry();
                    if (!layerRegistry.hasEntry(CHUNKY_KEY)) {
                        layerRegistry.register(CHUNKY_KEY, SimpleLayerProvider.builder(this.label)
                                .defaultHidden(hideByDefault)
                                .layerPriority(1)
                                .zIndex(priority)
                                .build());
                    }
                    SimpleLayerProvider chunkyLayerProvider = (SimpleLayerProvider) layerRegistry.get(CHUNKY_KEY);
                    chunkyLayerProvider.clearMarkers();
                    final Marker marker;
                    if (shape instanceof AbstractPolygon) {
                        AbstractPolygon polygon = (AbstractPolygon) shape;
                        double[] pointsX = polygon.pointsX();
                        double[] pointsZ = polygon.pointsZ();
                        if (pointsX.length != pointsZ.length) {
                            return;
                        }
                        Point[] points = new Point[pointsX.length + 1];
                        for (int i = 0; i < pointsX.length; ++i) {
                            points[i] = Point.of((int) pointsX[i], (int) pointsZ[i]);
                        }
                        points[pointsX.length] = Point.of((int) pointsX[0], (int) pointsZ[0]);
                        marker = Marker.polyline(points);
                    } else if (shape instanceof AbstractEllipse) {
                        AbstractEllipse ellipse = (AbstractEllipse) shape;
                        double[] center = ellipse.getCenter();
                        double[] radii = ellipse.getRadii();
                        Point centerPoint = Point.of((int) center[0], (int) center[1]);
                        if (ellipse instanceof Circle) {
                            marker = Marker.circle(centerPoint, radii[0]);
                        } else {
                            marker = ellipse(centerPoint, radii[0], radii[1]);
                        }
                    } else {
                        return;
                    }
                    MarkerOptions markerOptions = MarkerOptions.builder()
                            .stroke(true)
                            .strokeColor(this.color)
                            .strokeWeight(this.weight)
                            .fill(false)
                            .clickTooltip(this.label)
                            .build();
                    marker.markerOptions(markerOptions);
                    chunkyLayerProvider.addMarker(CHUNKY_KEY, marker);
                });
    }

    @Override
    public void removeShapeMarker(final World world) {
        pl3xMap.mapWorlds().stream()
                .filter(mapWorld -> mapWorld.uuid().equals(world.getUUID()))
                .findFirst()
                .ifPresent(pl3xWorld -> {
                    Registry<LayerProvider> layerRegistry = pl3xWorld.layerRegistry();
                    if (layerRegistry.hasEntry(CHUNKY_KEY)) {
                        ((SimpleLayerProvider) layerRegistry.get(CHUNKY_KEY)).clearMarkers();
                        layerRegistry.unregister(CHUNKY_KEY);
                    }
                });
    }

    @Override
    public void removeAllShapeMarkers() {
        pl3xMap.mapWorlds().forEach(pl3xWorld -> {
            Registry<LayerProvider> layerRegistry = pl3xWorld.layerRegistry();
            if (layerRegistry.hasEntry(CHUNKY_KEY)) {
                ((SimpleLayerProvider) layerRegistry.get(CHUNKY_KEY)).clearMarkers();
                layerRegistry.unregister(CHUNKY_KEY);
            }
        });
    }

    @Override
    public void setOptions(String label, String color, boolean hideByDefault, int priority, int weight) {
        super.setOptions(label, color, hideByDefault, priority, weight);
        this.hideByDefault = hideByDefault;
        this.priority = priority;
        this.weight = weight;
    }

    private Marker ellipse(Point center, double radiusX, double radiusZ) {
        int numPoints = 360;
        Point[] points = new Point[numPoints + 1];
        double segmentAngle = 2 * Math.PI / numPoints;
        for (int i = 0; i < numPoints; ++i) {
            double pointX = center.x() + Math.sin(segmentAngle * i) * radiusX;
            double pointZ = center.z() + Math.cos(segmentAngle * i) * radiusZ;
            points[i] = Point.of((int) pointX, (int) pointZ);
        }
        points[numPoints] = Point.of(center.x(), (int) (center.z() + radiusZ));
        return Marker.polyline(points);
    }
}
