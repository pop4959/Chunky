package org.popcraft.chunky.platform;

import org.popcraft.chunky.util.Coordinate;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TestWorld implements World {
    private final UUID uuid = UUID.randomUUID();

    @Override
    public String getName() {
        return "Test";
    }

    @Override
    public boolean isChunkGenerated(int x, int z) {
        return false;
    }

    @Override
    public CompletableFuture<Void> getChunkAtAsync(int x, int z) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public int getSeaLevel() {
        return 64;
    }

    @Override
    public Coordinate getSpawnCoordinate() {
        return new Coordinate(0, 0);
    }

    @Override
    public Border getWorldBorder() {
        return new Border() {
            @Override
            public Coordinate getCenter() {
                return new Coordinate(0, 0);
            }

            @Override
            public int getRadiusX() {
                return 1000;
            }

            @Override
            public int getRadiusZ() {
                return 1000;
            }

            @Override
            public String getShape() {
                return "square";
            }
        };
    }

    @Override
    public Optional<Path> getRegionDirectory() {
        return Optional.empty();
    }
}
