package org.popcraft.chunky.platform;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

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

    private static boolean classExists(final String clazz) {
        try {
            Class.forName(clazz);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
