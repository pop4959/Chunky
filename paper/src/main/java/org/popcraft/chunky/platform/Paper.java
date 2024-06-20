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

    private Paper() {
    }

    public static boolean isPaper() {
        return CONFIG_EXISTS;
    }

    public static CompletableFuture<Chunk> getChunkAtAsync(final World world, final int x, final int z) {
        return world.getChunkAtAsync(x, z, true);
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
