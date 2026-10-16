package org.popcraft.chunky.platform;

import io.papermc.paper.entity.TeleportFlag;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.world.chunk.ChunkGenerationState;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.command.suggestion.SuggestionProviders;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class Paper {
    private static final boolean CONFIG_EXISTS = classExists("com.destroystokyo.paper.PaperConfig") || classExists("io.papermc.paper.configuration.Configuration");
    private static final boolean CHUNK_GENERATION_STATE_EXISTS = methodExists(World.class, "getChunkGenerationStateAsync", int.class, int.class);
    private static final boolean LOAD_OUTDATED_CHUNKS = Boolean.getBoolean("chunky.loadOutdatedChunks");

    private Paper() {
    }

    public static boolean isPaper() {
        return CONFIG_EXISTS;
    }

    public static boolean hasChunkGenerationState() {
        return CHUNK_GENERATION_STATE_EXISTS;
    }

    public static CompletableFuture<Boolean> isChunkGeneratedAsync(final World world, final int x, final int z) {
        return world.getChunkGenerationStateAsync(x, z)
                .thenApply(state -> state == ChunkGenerationState.GENERATED
                        || state == ChunkGenerationState.OUTDATED && !LOAD_OUTDATED_CHUNKS)
                .exceptionally(ignored -> false);
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

    @SuppressWarnings("UnstableApiUsage")
    public static void registerCommand(JavaPlugin plugin, Chunky chunky, Function<CommandSender, Sender> commandSenderFunction, Function<Player, Sender> playerSenderFunction, String nodePermission) {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(
            event -> {
                final PaperChunkyCommand command = new PaperChunkyCommand(chunky, commandSenderFunction, playerSenderFunction, nodePermission);
                event.registrar().register(command.construct(new SuggestionProviders<>()).build(), "Generates chunks", List.of("cy"));
            }
        ));
    }

    private static boolean methodExists(final Class<?> clazz, final String name, final Class<?>... parameterTypes) {
        try {
            clazz.getMethod(name, parameterTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
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
