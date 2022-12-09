package org.popcraft.chunky.platform;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.popcraft.chunky.ChunkyFabric;
import org.popcraft.chunky.integration.Integration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FabricServer implements Server {
    private final ChunkyFabric plugin;
    private final MinecraftServer server;

    public FabricServer(final ChunkyFabric plugin, final MinecraftServer server) {
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
                .map(worldIdentifier -> server.getWorld(RegistryKey.of(RegistryKeys.WORLD, worldIdentifier)))
                .map(FabricWorld::new);
    }

    @Override
    public List<World> getWorlds() {
        final List<World> worlds = new ArrayList<>();
        server.getWorlds().forEach(world -> worlds.add(new FabricWorld(world)));
        return worlds;
    }

    @Override
    public Sender getConsole() {
        return new FabricSender(server.getCommandSource());
    }

    @Override
    public Collection<Player> getPlayers() {
        return server.getPlayerManager().getPlayerList().stream().map(FabricPlayer::new).collect(Collectors.toList());
    }

    @Override
    public Optional<Player> getPlayer(final String name) {
        return Optional.ofNullable(server.getPlayerManager().getPlayer(name)).map(FabricPlayer::new);
    }

    @Override
    public Config getConfig() {
        return plugin.getChunky().getConfig();
    }
}
