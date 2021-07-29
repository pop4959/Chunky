package org.popcraft.chunky.platform;

import org.popcraft.chunky.util.Coordinate;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SpongeWorld implements World {
    private ServerWorld world;

    public SpongeWorld(ServerWorld world) {
        this.world = world;
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
    public Coordinate getSpawnCoordinate() {
        Vector3i spawnLocation = world.properties().spawnPosition();
        return new Coordinate(spawnLocation.x(), spawnLocation.z());
    }

    @Override
    public Border getWorldBorder() {
        return new SpongeBorder(world.border());
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
        Path regionDirectory = world.directory().resolve("name");
        return Files.exists(regionDirectory) ? Optional.of(regionDirectory) : Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return world.equals(((SpongeWorld) o).world);
    }

    @Override
    public int hashCode() {
        return world.hashCode();
    }
}
