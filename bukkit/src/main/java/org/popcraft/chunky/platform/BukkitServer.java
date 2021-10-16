package org.popcraft.chunky.platform;

import org.popcraft.chunky.ChunkyBukkit;
import org.popcraft.chunky.integration.Integration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BukkitServer implements Server {
    private final ChunkyBukkit plugin;
    private final Map<String, Integration> integrations;

    public BukkitServer(ChunkyBukkit plugin) {
        this.plugin = plugin;
        this.integrations = new HashMap<>();
    }

    @Override
    public Map<String, Integration> getIntegrations() {
        return integrations;
    }

    @Override
    public Optional<World> getWorld(String name) {
        org.bukkit.World world = plugin.getServer().getWorld(name);
        return Optional.ofNullable(world == null ? null : new BukkitWorld(world));
    }

    @Override
    public List<World> getWorlds() {
        List<World> worlds = new ArrayList<>();
        plugin.getServer().getWorlds().forEach(world -> worlds.add(new BukkitWorld(world)));
        return worlds;
    }

    @Override
    public Sender getConsole() {
        return new BukkitSender(plugin.getServer().getConsoleSender());
    }

    @Override
    public Collection<Player> getPlayers() {
        return plugin.getServer().getOnlinePlayers().stream().map(BukkitPlayer::new).collect(Collectors.toList());
    }

    @Override
    public Optional<Player> getPlayer(String name) {
        return Optional.ofNullable(plugin.getServer().getPlayer(name)).map(BukkitPlayer::new);
    }

    @Override
    public Config getConfig() {
        return plugin.getChunky().getConfig();
    }
}
