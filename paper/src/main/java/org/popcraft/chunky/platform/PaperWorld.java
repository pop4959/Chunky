package org.popcraft.chunky.platform;

import io.papermc.paper.world.chunk.ChunkGenerationState;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.java.JavaPlugin;
import org.popcraft.chunky.ChunkyPaper;
import org.popcraft.chunky.platform.util.Location;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.Reflection;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class PaperWorld implements World {
    private static final boolean CHUNK_GENERATION_STATE_EXISTS = Reflection.methodExists(org.bukkit.World.class, "getChunkGenerationStateAsync", int.class, int.class);
    private static final boolean LOAD_OUTDATED_CHUNKS = Boolean.getBoolean("chunky.loadOutdatedChunks");
    private static final int TICKING_LOAD_DURATION = Input.tryInteger(System.getProperty("chunky.tickingLoadDuration")).orElse(0);
    private static final boolean AWAIT_TICKET_REMOVAL = Boolean.getBoolean("chunky.awaitTicketRemoval");
    private final JavaPlugin plugin = JavaPlugin.getPlugin(ChunkyPaper.class);
    private final org.bukkit.World world;
    private final Border worldBorder;

    public PaperWorld(final org.bukkit.World world) {
        this.world = world;
        this.worldBorder = new PaperBorder(world.getWorldBorder());
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
        if (CHUNK_GENERATION_STATE_EXISTS) {
            return ChunkGenerationStateCheck.isChunkGeneratedAsync(world, x, z);
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                return world.isChunkGenerated(x, z);
            } catch (CompletionException e) {
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Void> getChunkAtAsync(final int x, final int z) {
        final CompletableFuture<Void> chunkFuture = CompletableFuture.allOf(world.getChunkAtAsync(x, z, true));
        if (TICKING_LOAD_DURATION > 0) {
            final CompletableFuture<Void> removeTicketFuture = new CompletableFuture<>();
            chunkFuture.thenAccept(ignored -> {
                final Runnable addTicketTask = () -> world.addPluginChunkTicket(x, z, plugin);
                final Runnable removeTicketTask = () -> {
                    world.removePluginChunkTicket(x, z, plugin);
                    removeTicketFuture.complete(null);
                };
                final org.bukkit.Location location = new org.bukkit.Location(world, x << 4, 0, z << 4);
                plugin.getServer().getRegionScheduler().execute(plugin, location, addTicketTask);
                CompletableFuture.runAsync(() -> plugin.getServer().getRegionScheduler().execute(plugin, location, removeTicketTask), CompletableFuture.delayedExecutor(TICKING_LOAD_DURATION, TimeUnit.SECONDS));
            });
            if (AWAIT_TICKET_REMOVAL) {
                return removeTicketFuture;
            }
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
        if (!plugin.getServer().isOwnedByCurrentRegion(this.world, x >> 4, z >> 4)) {
            throw new IllegalStateException("Async getElevation call");
        } else {
            return getElevationForLocation(x, z);
        }
    }

    @Override
    public CompletableFuture<Integer> getElevationAtAsync(final int x, final int z) {
        final int chunkX = x >> 4;
        final int chunkZ = z >> 4;

        return world.getChunkAtAsync(chunkX, chunkZ, true).thenApplyAsync(ignored -> this.getElevationForLocation(x, z), getMainThreadExecutor(chunkX, chunkZ));
    }

    private int getElevationForLocation(final int x, final int z) {
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

    /**
     * {@return a new executor that executes tasks on the main thread}
     * <p>
     * Must not be passed around different threads, since it shortcuts to immediately running tasks if the calling thread is already a main thread.
     *
     * @param chunkX The chunk x coord (for Folia)
     * @param chunkZ The chunk z coord (for Folia)
     */
    private Executor getMainThreadExecutor(final int chunkX, final int chunkZ) {
        if (plugin.getServer().isOwnedByCurrentRegion(world, chunkX, chunkZ)) {
            return Runnable::run;
        } else {
            return runnable -> plugin.getServer().getRegionScheduler().execute(plugin, world, chunkX, chunkZ, runnable);
        }
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
        final org.bukkit.Location paperLocation = new org.bukkit.Location(world, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        world.playEffect(paperLocation, effectType, 0);
    }

    @Override
    public void playSound(final Player player, final String sound) {
        final String name = sound.toLowerCase();
        final Sound soundType = Registry.SOUND_EVENT.keyStream()
                .filter(key -> key.toString().equals(name) || key.getKey().equals(name) || key.getKey().replace('.', '_').equals(name))
                .findFirst()
                .map(Registry.SOUND_EVENT::get)
                .orElse(null);
        if (soundType == null) {
            return;
        }
        final Location location = player.getLocation();
        final org.bukkit.Location paperLocation = new org.bukkit.Location(world, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        world.playSound(paperLocation, soundType, 2f, 1f);
    }

    @Override
    public Optional<Path> getDirectory(final String name) {
        if (name == null) {
            return Optional.empty();
        }
        final NamespacedKey worldKey = world.getKey();
        final org.bukkit.World.Environment environment = world.getEnvironment();
        final String parent;
        final String dimension;
        if (org.bukkit.World.Environment.NETHER.equals(environment)) {
            parent = "DIM-1";
            dimension = "the_nether";
        } else if (org.bukkit.World.Environment.THE_END.equals(environment)) {
            parent = "DIM1";
            dimension = "the_end";
        } else if (org.bukkit.World.Environment.CUSTOM.equals(environment)) {
            parent = "";
            dimension = worldKey.getKey();
        } else {
            parent = "";
            dimension = "overworld";
        }
        final Path bukkitDir = world.getWorldFolder().toPath().resolve(parent).normalize().resolve(name);
        final Path vanillaDir = world.getWorldFolder().toPath().normalize().resolve(name);
        final Path paperDir = world.getWorldFolder().toPath().resolve("dimensions").resolve(worldKey.getNamespace()).resolve(dimension).resolve(name);
        final Path directory;
        if (Files.isDirectory(bukkitDir)) {
            directory = bukkitDir;
        } else if (Files.isDirectory(vanillaDir)) {
            directory = vanillaDir;
        } else {
            directory = paperDir;
        }
        return Files.isDirectory(directory) ? Optional.of(directory) : Optional.empty();
    }

    private static final class ChunkGenerationStateCheck {
        private static CompletableFuture<Boolean> isChunkGeneratedAsync(final org.bukkit.World world, final int x, final int z) {
            return world.getChunkGenerationStateAsync(x, z)
                    .thenApply(state -> state == ChunkGenerationState.GENERATED
                            || state == ChunkGenerationState.OUTDATED && !LOAD_OUTDATED_CHUNKS)
                    .exceptionally(ignored -> false);
        }
    }
}
