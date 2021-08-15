package org.popcraft.chunky.platform;

import org.bukkit.Bukkit;
import org.popcraft.chunky.ChunkyBukkit;
import org.popcraft.chunky.integration.Integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        org.bukkit.World world = Bukkit.getWorld(name);
        return Optional.ofNullable(world == null ? null : new BukkitWorld(world));
    }

    @Override
    public List<World> getWorlds() {
        List<World> worlds = new ArrayList<>();
        Bukkit.getWorlds().forEach(world -> worlds.add(new BukkitWorld(world)));
        return worlds;
    }

    @Override
    public Sender getConsoleSender() {
        return new BukkitSender(Bukkit.getConsoleSender());
    }

    @Override
    public Config getConfig() {
        return plugin.getChunky().getConfig();
    }
}
