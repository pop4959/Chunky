package org.popcraft.chunky.platform;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.popcraft.chunky.ChunkyFabric;
import org.popcraft.chunky.integration.Integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        Identifier worldIdentifier = Identifier.tryParse(name);
        if (worldIdentifier == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(server.getWorld(RegistryKey.of(Registry.WORLD_KEY, worldIdentifier)))
                .map(FabricWorld::new);
    }

    @Override
    public List<World> getWorlds() {
        List<World> worlds = new ArrayList<>();
        server.getWorlds().forEach(world -> worlds.add(new FabricWorld(world)));
        return worlds;
    }

    @Override
    public Sender getConsoleSender() {
        return new FabricSender(server.getCommandSource());
    }

    @Override
    public Config getConfig() {
        return plugin.getChunky().getConfig();
    }
}
