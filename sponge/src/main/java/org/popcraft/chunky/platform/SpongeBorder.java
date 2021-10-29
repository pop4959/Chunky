package org.popcraft.chunky.platform;

import org.popcraft.chunky.platform.util.Vector2;
import org.popcraft.chunky.shape.ShapeType;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector2d;

public class SpongeBorder implements Border {
    private final ServerWorld serverWorld;

    public SpongeBorder(ServerWorld serverWorld) {
        this.serverWorld = serverWorld;
    }

    @Override
    public Vector2 getCenter() {
        Vector2d center = serverWorld.border().center();
        return Vector2.of(center.x(), center.y());
    }

    @Override
    public double getRadiusX() {
        return serverWorld.border().diameter() / 2d;
    }

    @Override
    public double getRadiusZ() {
        return serverWorld.border().diameter() / 2d;
    }

    @Override
    public String getShape() {
        return ShapeType.SQUARE;
    }
}
