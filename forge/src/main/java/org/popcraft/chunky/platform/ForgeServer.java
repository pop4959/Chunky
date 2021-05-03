package org.popcraft.chunky.platform;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.popcraft.chunky.ChunkyForge;
import org.popcraft.chunky.integration.Integration;
import org.popcraft.chunky.platform.impl.SimpleScheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ForgeServer implements Server {
    private ChunkyForge plugin;
    private MinecraftServer server;
    private Scheduler scheduler;

    public ForgeServer(ChunkyForge plugin, MinecraftServer server) {
        this.plugin = plugin;
        this.server = server;
        this.scheduler = new SimpleScheduler();
    }

    @Override
    public Map<String, Integration> getIntegrations() {
        return Collections.emptyMap();
    }

    @Override
    public Optional<World> getWorld(String name) {
        ResourceLocation resourceLocation = ResourceLocation.tryCreate(name);
        if (resourceLocation == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(server.getWorld(RegistryKey.getOrCreateKey(Registry.WORLD_KEY, resourceLocation)))
                .map(ForgeWorld::new);
    }

    @Override
    public List<World> getWorlds() {
        List<World> worlds = new ArrayList<>();
        server.getWorlds().forEach(world -> worlds.add(new ForgeWorld(world)));
        return worlds;
    }

    @Override
    public Sender getConsoleSender() {
        return new ForgeSender(server.getCommandSource());
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public Config getConfig() {
        return plugin.getChunky().getConfig();
    }
}
