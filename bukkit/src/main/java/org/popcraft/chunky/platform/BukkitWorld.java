package org.popcraft.chunky.platform;

import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.java.JavaPlugin;
import org.popcraft.chunky.ChunkyBukkit;
import org.popcraft.chunky.platform.util.Location;
import org.popcraft.chunky.util.Input;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

public class BukkitWorld implements World {
    private static final boolean IS_GENERATED_SUPPORTED;
    private static final int TICKING_LOAD_DURATION = Input.tryInteger(System.getProperty("chunky.tickingLoadDuration")).orElse(0);
    private static final boolean AWAIT_TICKET_REMOVAL = Boolean.getBoolean("chunky.awaitTicketRemoval");
    private final JavaPlugin plugin = JavaPlugin.getPlugin(ChunkyBukkit.class);
    private final org.bukkit.World world;
    private final Border worldBorder;

    static {
        boolean isGeneratedSupported;
        try {
            Chunk.class.getMethod("isGenerated");
            isGeneratedSupported = true;
        } catch (NoSuchMethodException e) {
            isGeneratedSupported = false;
        }
        IS_GENERATED_SUPPORTED = isGeneratedSupported;
    }

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

    public org.bukkit.World getBukkitWorld() {
        return this.world;
    }

    @Override
    public CompletableFuture<Boolean> isChunkGenerated(final int x, final int z) {
        if (Paper.isPaper()) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return world.isChunkGenerated(x, z);
                } catch (CompletionException e) {
                    return false;
                }
            });
        } else {
            if (IS_GENERATED_SUPPORTED) {
                return CompletableFuture.completedFuture(world.getChunkAt(x, z, false).isGenerated());
            } else {
                return CompletableFuture.completedFuture(false);
            }
        }
    }

    @Override
    public CompletableFuture<Void> getChunkAtAsync(final int x, final int z) {
        final CompletableFuture<Void> chunkFuture;
        if (Paper.isPaper()) {
            chunkFuture = CompletableFuture.allOf(Paper.getChunkAtAsync(world, x, z));
        } else {
            chunkFuture = CompletableFuture.allOf(CompletableFuture.completedFuture(world.getChunkAt(x, z)));
        }
        if (TICKING_LOAD_DURATION > 0) {
            final CompletableFuture<Void> removeTicketFuture = new CompletableFuture<>();
            chunkFuture.thenAccept(ignored -> {
                final Runnable addTicketTask = () -> world.addPluginChunkTicket(x, z, plugin);
                final Runnable removeTicketTask = () -> {
                    world.removePluginChunkTicket(x, z, plugin);
                    removeTicketFuture.complete(null);
                };
                if (Folia.isFolia()) {
                    final org.bukkit.Location location = new org.bukkit.Location(world, x << 4, 0, z << 4);
                    Folia.schedule(plugin, location, addTicketTask);
                    CompletableFuture.runAsync(() -> Folia.schedule(plugin, location, removeTicketTask), CompletableFuture.delayedExecutor(TICKING_LOAD_DURATION, TimeUnit.SECONDS));
                } else {
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, addTicketTask);
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, removeTicketTask, TICKING_LOAD_DURATION * 20L);
                }
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
        final org.bukkit.Location location = new org.bukkit.Location(world, x, 0, z);
        if (Folia.isFolia() && !Folia.isTickThread(location)) {
            CompletableFuture<Integer> future =
                CompletableFuture.supplyAsync(() -> getElevationForLocation(x, z), command
                    -> Folia.schedule(plugin, location, command));
            try {
                runManagedBlock(world, future);
                return future.get();
            } catch (Exception e) {
                throw new RuntimeException("Couldn't run managed block for fetching elevation", e);
            }
        } else {
            return getElevationForLocation(x, z);
        }
    }

    private static void runManagedBlock(org.bukkit.World world, CompletableFuture<Integer> toComplete) throws Exception {
        Object runningLevel = world.getClass().getMethod("getHandle").invoke(world);
        Object currentWorldData = runningLevel.getClass().getMethod("getCurrentWorldData").invoke(world);

        java.lang.reflect.Field worldField = currentWorldData.getClass().getField("world");
        Object serverLevel = worldField.get(currentWorldData);

        java.lang.reflect.Field chunkSourceField = serverLevel.getClass().getField("chunkSource");
        Object chunkSource = chunkSourceField.get(serverLevel);

        java.lang.reflect.Field mainThreadProcessorField = chunkSource.getClass().getDeclaredField("mainThreadProcessor");
        mainThreadProcessorField.setAccessible(true);
        Object mainThreadProcessor = mainThreadProcessorField.get(chunkSource);

        java.lang.reflect.Method managedBlockMethod = mainThreadProcessor.getClass().getMethod("managedBlock", java.util.function.BooleanSupplier.class);
        managedBlockMethod.invoke(mainThreadProcessor, (java.util.function.BooleanSupplier) toComplete::isDone);
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
        if (name == null) {
            return Optional.empty();
        }
        final org.bukkit.World.Environment environment = world.getEnvironment();
        final String parent;
        if (org.bukkit.World.Environment.NETHER.equals(environment)) {
            parent = "DIM-1";
        } else if (org.bukkit.World.Environment.THE_END.equals(environment)) {
            parent = "DIM1";
        } else {
            parent = "";
        }
        final Path directory = world.getWorldFolder().toPath().resolve(parent).normalize().resolve(name);
        return Files.isDirectory(directory) ? Optional.of(directory) : Optional.empty();
    }
}
