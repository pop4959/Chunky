package org.popcraft.chunky.platform;

import org.popcraft.chunky.ChunkyPaper;
import org.popcraft.chunky.integration.Integration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PaperServer implements Server {
    private final ChunkyPaper plugin;
    private final Map<String, Integration> integrations;

    public PaperServer(final ChunkyPaper plugin) {
        this.plugin = plugin;
        this.integrations = new HashMap<>();
    }

    @Override
    public Map<String, Integration> getIntegrations() {
        return integrations;
    }

    @Override
    public Optional<World> getWorld(final String name) {
        final org.bukkit.World world = plugin.getServer().getWorld(name);
        return Optional.ofNullable(world == null ? null : new PaperWorld(world));
    }

    @Override
    public List<World> getWorlds() {
        final List<World> worlds = new ArrayList<>();
        plugin.getServer().getWorlds().forEach(world -> worlds.add(new PaperWorld(world)));
        return worlds;
    }

    @Override
    public int getMaxWorldSize() {
        return plugin.getServer().getMaxWorldSize();
    }

    @Override
    public Sender getConsole() {
        return new PaperSender(plugin.getServer().getConsoleSender());
    }

    @Override
    public Collection<Player> getPlayers() {
        final Collection<Player> players = new ArrayList<>();
        plugin.getServer().getOnlinePlayers().forEach(player -> players.add(new PaperPlayer(player)));
        return players;
    }

    @Override
    public Optional<Player> getPlayer(final String name) {
        return Optional.ofNullable(plugin.getServer().getPlayer(name)).map(PaperPlayer::new);
    }

    @Override
    public Config getConfig() {
        return plugin.getChunky().getConfig();
    }
}
