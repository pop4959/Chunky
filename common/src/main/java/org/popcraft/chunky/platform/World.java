package org.popcraft.chunky.platform;

import org.popcraft.chunky.platform.util.Location;

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

    Location getSpawn();

    Border getWorldBorder();

    Optional<Path> getEntitiesDirectory();

    Optional<Path> getPOIDirectory();

    Optional<Path> getRegionDirectory();
}
