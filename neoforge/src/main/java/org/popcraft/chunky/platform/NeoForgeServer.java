package org.popcraft.chunky.platform;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.popcraft.chunky.ChunkyNeoForge;
import org.popcraft.chunky.integration.Integration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class NeoForgeServer implements Server {
    private final ChunkyNeoForge plugin;
    private final MinecraftServer server;

    public NeoForgeServer(final ChunkyNeoForge plugin, final MinecraftServer server) {
        this.plugin = plugin;
        this.server = server;
    }

    @Override
    public Map<String, Integration> getIntegrations() {
        return Map.of();
    }

    @Override
    public Optional<World> getWorld(final String name) {
        return Optional.ofNullable(ResourceLocation.tryParse(name))
                .map(resourceLocation -> server.getLevel(ResourceKey.create(Registries.DIMENSION, resourceLocation)))
                .or(() -> {
                    for (final ServerLevel level : server.getAllLevels()) {
                        if (name.equals(level.dimension().location().getPath())) {
                            return Optional.of(level);
                        }
                    }
                    return Optional.empty();
                })
                .map(NeoForgeWorld::new);
    }

    @Override
    public List<World> getWorlds() {
        final List<World> worlds = new ArrayList<>();
        server.getAllLevels().forEach(world -> worlds.add(new NeoForgeWorld(world)));
        return worlds;
    }

    @Override
    public Sender getConsole() {
        return new NeoForgeSender(server.createCommandSourceStack());
    }

    @Override
    public Collection<Player> getPlayers() {
        return server.getPlayerList().getPlayers().stream().map(NeoForgePlayer::new).collect(Collectors.toList());
    }

    @Override
    public Optional<Player> getPlayer(final String name) {
        return Optional.ofNullable(server.getPlayerList().getPlayerByName(name)).map(NeoForgePlayer::new);
    }

    @Override
    public Config getConfig() {
        return plugin.getChunky().getConfig();
    }
}
