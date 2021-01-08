package org.popcraft.chunky.platform;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.popcraft.chunky.ChunkyFabric;
import org.popcraft.chunky.integration.Integration;
import org.popcraft.chunky.platform.watchdog.FabricWatchdogs;
import org.popcraft.chunky.platform.watchdog.Watchdogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FabricServer implements Server {
    private ChunkyFabric plugin;
    private MinecraftServer server;
    private Scheduler scheduler;
    private Watchdogs watchdogs;

    public FabricServer(ChunkyFabric plugin, MinecraftServer server) {
        this.plugin = plugin;
        this.server = server;
        this.scheduler = new FabricScheduler();
        this.watchdogs = new FabricWatchdogs(plugin);
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
        ServerWorld serverWorld = server.getWorld(RegistryKey.of(Registry.DIMENSION, worldIdentifier));
        if (serverWorld == null) {
            return Optional.empty();
        }
        return Optional.of(new FabricWorld(serverWorld));
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
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public Config getConfig() {
        return plugin.getChunky().getConfig();
    }

    @Override
    public Watchdogs getWatchdogs() {
        return watchdogs;
    }
}
