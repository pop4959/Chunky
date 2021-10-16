package org.popcraft.chunky.platform;

import org.popcraft.chunky.platform.util.Location;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SpongeWorld implements World {
    private final ServerWorld world;
    private final Border worldBorder;

    public SpongeWorld(ServerWorld world) {
        this.world = world;
        this.worldBorder = new SpongeBorder(world);
    }

    @Override
    public String getName() {
        return world.key().asString();
    }

    @Override
    public boolean isChunkGenerated(int x, int z) {
        return world.hasChunk(x, 0, z);
    }

    @Override
    public CompletableFuture<Void> getChunkAtAsync(int x, int z) {
        world.loadChunk(x, 0, z, true);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public UUID getUUID() {
        return world.uniqueId();
    }

    @Override
    public int getSeaLevel() {
        return world.seaLevel();
    }

    @Override
    public Location getSpawn() {
        Vector3i spawn = world.properties().spawnPosition();
        return new Location(this, spawn.x(), spawn.y(), spawn.z(), 0, 0);
    }

    @Override
    public Border getWorldBorder() {
        return worldBorder;
    }

    @Override
    public Optional<Path> getEntitiesDirectory() {
        return getDirectory("entities");
    }

    @Override
    public Optional<Path> getPOIDirectory() {
        return getDirectory("poi");
    }

    @Override
    public Optional<Path> getRegionDirectory() {
        return getDirectory("region");
    }

    private Optional<Path> getDirectory(final String name) {
        if (name == null) {
            return Optional.empty();
        }
        Path regionDirectory = world.directory().resolve(name);
        return Files.exists(regionDirectory) ? Optional.of(regionDirectory) : Optional.empty();
    }
}
