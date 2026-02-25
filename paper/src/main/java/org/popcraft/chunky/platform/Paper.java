package org.popcraft.chunky.platform;

import io.papermc.paper.entity.TeleportFlag;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.concurrent.CompletableFuture;

public final class Paper {
    private static final boolean CONFIG_EXISTS = classExists("com.destroystokyo.paper.PaperConfig") || classExists("io.papermc.paper.configuration.Configuration");
    /**
     * When {@code true}, Chunky requests a chunk unload immediately after generation
     * to evict the chunk from the JVM heap and keep memory pressure flat.
     * Enable with: {@code -Dchunky.unloadAfterGenerate=true}
     */
    static final boolean UNLOAD_AFTER_GENERATE = Boolean.getBoolean("chunky.unloadAfterGenerate");

    private Paper() {
    }

    public static boolean isPaper() {
        return CONFIG_EXISTS;
    }

    public static CompletableFuture<Chunk> getChunkAtAsync(final World world, final int x, final int z) {
        // urgent=true places the request in Paper's high-priority chunk I/O queue,
        // significantly improving pre-generation throughput vs. the normal-priority queue.
        return world.getChunkAtAsync(x, z, true, true);
    }

    /**
     * Requests an immediate chunk eviction after generation to reclaim JVM heap.
     * Only effective when enabled via {@code -Dchunky.unloadAfterGenerate=true}.
     * Paper's chunk system will honour the request asynchronously after the chunk
     * is confirmed written to disk, so there is no risk of data loss.
     */
    public static void unloadChunkRequest(final World world, final int x, final int z) {
        if (UNLOAD_AFTER_GENERATE) {
            world.unloadChunkRequest(x, z);
        }
    }

    public static CompletableFuture<Boolean> teleportAsync(final Entity entity, final Location location) {
        return entity.teleportAsync(location);
    }

    public static CompletableFuture<Boolean> teleportAsyncWithPassengers(final Entity entity, final Location location) {
        return entity.teleportAsync(location, PlayerTeleportEvent.TeleportCause.PLUGIN, TeleportFlag.EntityState.RETAIN_PASSENGERS, TeleportFlag.EntityState.RETAIN_VEHICLE);
    }

    private static boolean classExists(final String clazz) {
        try {
            Class.forName(clazz);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
