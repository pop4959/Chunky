package org.popcraft.chunky.platform;

import org.popcraft.chunky.ChunkySponge;
import org.popcraft.chunky.integration.Integration;
import org.spongepowered.api.ResourceKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SpongeServer implements Server {
    private final ChunkySponge plugin;
    private final Map<String, Integration> integrations;

    public SpongeServer(final ChunkySponge plugin) {
        this.plugin = plugin;
        this.integrations = new HashMap<>();
    }

    @Override
    public Map<String, Integration> getIntegrations() {
        return integrations;
    }

    @Override
    public Optional<World> getWorld(final String name) {
        return plugin.getGame().server().worldManager().world(ResourceKey.resolve(name)).map(SpongeWorld::new);
    }

    @Override
    public List<World> getWorlds() {
        final List<World> worlds = new ArrayList<>();
        plugin.getGame().server().worldManager().worlds().forEach(world -> worlds.add(new SpongeWorld(world)));
        return worlds;
    }

    @Override
    public Sender getConsole() {
        return new SpongeSender(plugin.getGame().systemSubject());
    }

    @Override
    public Collection<Player> getPlayers() {
        final Collection<Player> players = new ArrayList<>();
        plugin.getGame().server().onlinePlayers().forEach(player -> players.add(new SpongePlayer(player)));
        return players;
    }

    @Override
    public Optional<Player> getPlayer(final String name) {
        return plugin.getGame().server().player(name).map(SpongePlayer::new);
    }

    @Override
    public Config getConfig() {
        return plugin.getChunky().getConfig();
    }
}
