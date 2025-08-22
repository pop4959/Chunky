package org.popcraft.chunky.platform;

import org.popcraft.chunky.ChunkyBukkit;
import org.popcraft.chunky.integration.Integration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BukkitServer implements Server {
    private final ChunkyBukkit plugin;
    private final Map<String, Integration> integrations;

    public BukkitServer(final ChunkyBukkit plugin) {
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
        return Optional.ofNullable(world == null ? null : new BukkitWorld(world));
    }

    @Override
    public List<World> getWorlds() {
        final List<World> worlds = new ArrayList<>();
        plugin.getServer().getWorlds().forEach(world -> worlds.add(new BukkitWorld(world)));
        return worlds;
    }

    @Override
    public int getMaxWorldSize() {
        return plugin.getServer().getMaxWorldSize();
    }

    @Override
    public Sender getConsole() {
        return new BukkitSender(plugin.getServer().getConsoleSender());
    }

    @Override
    public Collection<Player> getPlayers() {
        final Collection<Player> players = new ArrayList<>();
        plugin.getServer().getOnlinePlayers().forEach(player -> players.add(new BukkitPlayer(player)));
        return players;
    }

    @Override
    public Optional<Player> getPlayer(final String name) {
        return Optional.ofNullable(plugin.getServer().getPlayer(name)).map(BukkitPlayer::new);
    }

    @Override
    public Config getConfig() {
        return plugin.getChunky().getConfig();
    }
}
