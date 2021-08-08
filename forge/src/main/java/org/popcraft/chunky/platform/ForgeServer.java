package org.popcraft.chunky.platform;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.popcraft.chunky.ChunkyForge;
import org.popcraft.chunky.integration.Integration;
import org.popcraft.chunky.platform.impl.SimpleScheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ForgeServer implements Server {
    private final ChunkyForge plugin;
    private final MinecraftServer server;
    private final Scheduler scheduler;

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
        ResourceLocation resourceLocation = ResourceLocation.tryParse(name);
        if (resourceLocation == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(server.getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, resourceLocation)))
                .map(ForgeWorld::new);
    }

    @Override
    public List<World> getWorlds() {
        List<World> worlds = new ArrayList<>();
        server.getAllLevels().forEach(world -> worlds.add(new ForgeWorld(world)));
        return worlds;
    }

    @Override
    public Sender getConsoleSender() {
        return new ForgeSender(server.createCommandSourceStack());
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
