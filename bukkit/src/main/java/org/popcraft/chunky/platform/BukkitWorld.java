package org.popcraft.chunky.platform;

import io.papermc.lib.PaperLib;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.java.JavaPlugin;
import org.popcraft.chunky.ChunkyBukkit;
import org.popcraft.chunky.platform.util.Location;
import org.popcraft.chunky.util.Input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

public class BukkitWorld implements World {
    private static final int TICKING_LOAD_DURATION = Input.tryInteger(System.getProperty("chunky.tickingLoadDuration")).orElse(0);
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
    public CompletableFuture<Boolean> isChunkGenerated(final int x, final int z) {
        if (PaperLib.isPaper()) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return PaperLib.isChunkGenerated(world, x, z);
                } catch (CompletionException e) {
                    return false;
                }
            });
        } else {
            return CompletableFuture.completedFuture(false);
        }
    }

    @Override
    public CompletableFuture<Void> getChunkAtAsync(final int x, final int z) {
        final CompletableFuture<Void> chunkFuture = CompletableFuture.allOf(PaperLib.getChunkAtAsync(world, x, z));
        if (TICKING_LOAD_DURATION > 0) {
            final JavaPlugin plugin = JavaPlugin.getPlugin(ChunkyBukkit.class);
            chunkFuture.thenAccept(ignored -> {
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> world.addPluginChunkTicket(x, z, plugin));
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> world.removePluginChunkTicket(x, z, plugin), TICKING_LOAD_DURATION * 20L);
            });
        }
        return chunkFuture;
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
        final int height = world.getHighestBlockYAt(x, z) + 1;
        final int logicalHeight = world.getLogicalHeight();
        if (height >= logicalHeight) {
            Block block = world.getBlockAt(x, logicalHeight, z);
            int air = 0;
            while (block.getY() > world.getMinHeight()) {
                block = block.getRelative(BlockFace.DOWN);
                final Material type = block.getType();
                if (type.isSolid() && air > 1) {
                    return block.getY() + 1;
                }
                air = type.isAir() ? air + 1 : 0;
            }
        }
        return height;
    }

    @Override
    public int getMaxElevation() {
        return world.getLogicalHeight();
    }

    @Override
    public void playEffect(final Player player, final String effect) {
        final Effect effectType;
        try {
            effectType = Effect.valueOf(effect.toUpperCase());
        } catch (IllegalArgumentException e) {
            return;
        }
        final Location location = player.getLocation();
        final org.bukkit.Location bukkitLocation = new org.bukkit.Location(world, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        world.playEffect(bukkitLocation, effectType, 0);
    }

    @Override
    public void playSound(final Player player, final String sound) {
        final Sound soundType;
        try {
            soundType = Sound.valueOf(sound.toUpperCase());
        } catch (IllegalArgumentException e) {
            return;
        }
        final Location location = player.getLocation();
        final org.bukkit.Location bukkitLocation = new org.bukkit.Location(world, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        world.playSound(bukkitLocation, soundType, 2f, 1f);
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
