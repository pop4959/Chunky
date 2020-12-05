package org.popcraft.chunky.platform;

import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.popcraft.chunky.util.Coordinate;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BukkitWorld implements World {
    private org.bukkit.World world;
    private Border worldBorder;

    public BukkitWorld(org.bukkit.World world) {
        this.world = world;
        this.worldBorder = new BukkitBorder(world.getWorldBorder());
    }

    @Override
    public String getName() {
        return world.getName();
    }

    @Override
    public boolean isChunkGenerated(int x, int z) {
        return PaperLib.isPaper() && PaperLib.isChunkGenerated(world, x, z);
    }

    @Override
    public CompletableFuture<Void> getChunkAtAsync(int x, int z) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        PaperLib.getChunkAtAsync(world, x, z).thenRun(() -> future.complete(null));
        return future;
    }

    @Override
    public UUID getUUID() {
        return world.getUID();
    }

    @Override
    public Border getWorldBorder() {
        return worldBorder;
    }

    @Override
    public Coordinate getSpawnCoordinate() {
        Location spawnLocation = world.getSpawnLocation();
        return new Coordinate(spawnLocation.getBlockX(), spawnLocation.getBlockZ());
    }

    @Override
    public int getSeaLevel() {
        return world.getSeaLevel();
    }
}
