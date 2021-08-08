package org.popcraft.chunky.platform;

import org.popcraft.chunky.ChunkySponge;
import org.popcraft.chunky.integration.Integration;
import org.popcraft.chunky.platform.impl.SimpleScheduler;
import org.spongepowered.api.ResourceKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpongeServer implements Server {
    private final ChunkySponge plugin;
    private final Map<String, Integration> integrations;
    private final Scheduler scheduler;

    public SpongeServer(ChunkySponge plugin) {
        this.plugin = plugin;
        this.integrations = new HashMap<>();
        this.scheduler = new SimpleScheduler();
    }

    @Override
    public Map<String, Integration> getIntegrations() {
        return integrations;
    }

    @Override
    public Optional<World> getWorld(String name) {
        return plugin.getGame().server().worldManager().world(ResourceKey.resolve(name)).map(SpongeWorld::new);
    }

    @Override
    public List<World> getWorlds() {
        return plugin.getGame().server().worldManager().worlds().stream().map(SpongeWorld::new).collect(Collectors.toList());
    }

    @Override
    public Sender getConsoleSender() {
        return new SpongeSender(plugin.getGame().server());
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
