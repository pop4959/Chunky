package org.popcraft.chunky.platform;

import org.popcraft.chunky.platform.util.Location;
import org.popcraft.chunky.platform.util.Vector3;

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

    int getElevation(int x, int z);

    void playEffect(Player player, String effect);

    void playSound(Player player, String sound);

    Optional<Path> getEntitiesDirectory();

    Optional<Path> getPOIDirectory();

    Optional<Path> getRegionDirectory();
}
