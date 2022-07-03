package org.popcraft.chunky.platform;

import io.papermc.lib.PaperLib;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.popcraft.chunky.platform.util.Location;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

public class BukkitWorld implements World {
    private final org.bukkit.World world;
    private final Border worldBorder;

    public BukkitWorld(final org.bukkit.World world) {
        this.world = world;
        this.worldBorder = new BukkitBorder(world.getWorldBorder());
    }

    @Override
    public String getName() {
        return world.getName();
    }

    @Override
    public String getKey() {
        return world.getKey().toString();
    }

    @Override
    public boolean isChunkGenerated(final int x, final int z) {
        try {
            return PaperLib.isPaper() && PaperLib.isChunkGenerated(world, x, z);
        } catch (CompletionException e) {
            return false;
        }
    }

    @Override
    public CompletableFuture<Void> getChunkAtAsync(final int x, final int z) {
        return CompletableFuture.allOf(PaperLib.getChunkAtAsync(world, x, z));
    }

    @Override
    public UUID getUUID() {
        return world.getUID();
    }

    @Override
    public int getSeaLevel() {
        return world.getSeaLevel();
    }

    @Override
    public Location getSpawn() {
        final org.bukkit.Location spawnLocation = world.getSpawnLocation();
        return new Location(this, spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ(), spawnLocation.getYaw(), spawnLocation.getPitch());
    }

    @Override
    public Border getWorldBorder() {
        return worldBorder;
    }

    @Override
    public int getElevation(final int x, final int z) {
        return world.getHighestBlockYAt(x, z);
    }

    @Override
    public void playEffect(final Player player, final String effect) {
        try {
            final Location location = player.getLocation();
            final org.bukkit.Location bukkitLocation = new org.bukkit.Location(world, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
            world.playEffect(bukkitLocation, Effect.valueOf(effect.toUpperCase()), 0);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    public void playSound(final Player player, final String sound) {
        try {
            final Location location = player.getLocation();
            final org.bukkit.Location bukkitLocation = new org.bukkit.Location(world, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
            world.playSound(bukkitLocation, Sound.valueOf(sound.toUpperCase()), 2f, 1f);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    public Optional<Path> getDirectory(final String name) {
        if (name != null) {
            try (Stream<Path> paths = Files.walk(world.getWorldFolder().toPath())) {
                return paths.filter(Files::isDirectory)
                        .filter(path -> name.equals(path.getFileName().toString()))
                        .findFirst();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }
}
