package org.popcraft.chunky.platform;

import org.popcraft.chunky.shape.ShapeType;
import org.popcraft.chunky.util.Coordinate;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector2d;

public class SpongeBorder implements Border {
    private final ServerWorld serverWorld;

    public SpongeBorder(ServerWorld serverWorld) {
        this.serverWorld = serverWorld;
    }

    @Override
    public Coordinate getCenter() {
        Vector2d center = serverWorld.border().center();
        return new Coordinate(center.x(), center.y());
    }

    @Override
    public void setCenter(Coordinate coordinate) {
        serverWorld.setBorder(serverWorld.border().toBuilder().center(coordinate.getX(), coordinate.getZ()).build());
    }

    @Override
    public double getRadiusX() {
        return serverWorld.border().diameter() / 2d;
    }

    @Override
    public void setRadiusX(double radiusX) {
        serverWorld.setBorder(serverWorld.border().toBuilder().targetDiameter(radiusX * 2).build());
    }

    @Override
    public double getRadiusZ() {
        return serverWorld.border().diameter() / 2d;
    }

    @Override
    public void setRadiusZ(double radiusZ) {
        serverWorld.setBorder(serverWorld.border().toBuilder().targetDiameter(radiusZ * 2).build());
    }

    @Override
    public String getShape() {
        return ShapeType.SQUARE;
    }
}
