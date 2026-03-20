package org.popcraft.chunky.platform;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerInterface;
import net.minecraft.server.level.ServerLevel;
import org.popcraft.chunky.ChunkyForge;
import org.popcraft.chunky.integration.Integration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ForgeServer implements Server {
    private final ChunkyForge plugin;
    private final MinecraftServer server;

    public ForgeServer(final ChunkyForge plugin, final MinecraftServer server) {
        this.plugin = plugin;
        this.server = server;
    }

    @Override
    public Map<String, Integration> getIntegrations() {
        return Map.of();
    }

    @Override
    public Optional<World> getWorld(final String name) {
        return Optional.ofNullable(Identifier.tryParse(name))
                .map(resourceLocation -> server.getLevel(ResourceKey.create(Registries.DIMENSION, resourceLocation)))
                .or(() -> {
                    for (final ServerLevel level : server.getAllLevels()) {
                        if (name.equals(level.dimension().identifier().getPath())) {
                            return Optional.of(level);
                        }
                    }
                    return Optional.empty();
                })
                .map(ForgeWorld::new);
    }

    @Override
    public List<World> getWorlds() {
        final List<World> worlds = new ArrayList<>();
        server.getAllLevels().forEach(world -> worlds.add(new ForgeWorld(world)));
        return worlds;
    }

    @Override
    public int getMaxWorldSize() {
        if (server instanceof final ServerInterface serverInterface) {
            return serverInterface.getProperties().maxWorldSize;
        } else {
            return server.getAbsoluteMaxWorldSize();
        }
    }

    @Override
    public Sender getConsole() {
        return new ForgeSender(server.createCommandSourceStack());
    }

    @Override
    public Collection<Player> getPlayers() {
        return server.getPlayerList().getPlayers().stream().map(ForgePlayer::new).collect(Collectors.toList());
    }

    @Override
    public Optional<Player> getPlayer(final String name) {
        return Optional.ofNullable(server.getPlayerList().getPlayerByName(name)).map(ForgePlayer::new);
    }

    @Override
    public Config getConfig() {
        return plugin.getChunky().getConfig();
    }
}
