package org.popcraft.chunky.platform;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.popcraft.chunky.ChunkyFabric;
import org.popcraft.chunky.integration.Integration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FabricServer implements Server {
    private final ChunkyFabric plugin;
    private final MinecraftServer server;

    public FabricServer(ChunkyFabric plugin, MinecraftServer server) {
        this.plugin = plugin;
        this.server = server;
    }

    @Override
    public Map<String, Integration> getIntegrations() {
        return Collections.emptyMap();
    }

    @Override
    public Optional<World> getWorld(String name) {
        return Optional.ofNullable(Identifier.tryParse(name))
                .map(worldIdentifier -> server.getWorld(RegistryKey.of(Registry.WORLD_KEY, worldIdentifier)))
                .map(FabricWorld::new);
    }

    @Override
    public List<World> getWorlds() {
        List<World> worlds = new ArrayList<>();
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
    public Optional<Player> getPlayer(String name) {
        return Optional.ofNullable(server.getPlayerManager().getPlayer(name)).map(FabricPlayer::new);
    }

    @Override
    public Config getConfig() {
        return plugin.getChunky().getConfig();
    }
}
