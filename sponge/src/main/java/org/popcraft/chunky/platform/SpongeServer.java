package org.popcraft.chunky.platform;

import org.popcraft.chunky.ChunkySponge;
import org.popcraft.chunky.integration.Integration;
import org.spongepowered.api.ResourceKey;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpongeServer implements Server {
    private final ChunkySponge plugin;
    private final Map<String, Integration> integrations;

    public SpongeServer(ChunkySponge plugin) {
        this.plugin = plugin;
        this.integrations = new HashMap<>();
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
    public Sender getConsole() {
        return new SpongeSender(plugin.getGame().systemSubject());
    }

    @Override
    public Collection<Player> getPlayers() {
        return plugin.getGame().server().onlinePlayers().stream().map(SpongePlayer::new).collect(Collectors.toList());
    }

    @Override
    public Optional<Player> getPlayer(String name) {
        return plugin.getGame().server().player(name).map(SpongePlayer::new);
    }

    @Override
    public Config getConfig() {
        return plugin.getChunky().getConfig();
    }
}
