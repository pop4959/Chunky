package org.popcraft.chunky.platform;

import org.popcraft.chunky.util.Coordinate;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface World {
    String getName();

    boolean isChunkGenerated(int x, int z);

    CompletableFuture<Void> getChunkAtAsync(int x, int z);

    UUID getUUID();

    int getSeaLevel();

    Coordinate getSpawnCoordinate();

    Border getWorldBorder();

    Optional<Path> getRegionDirectory();
}
