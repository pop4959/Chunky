package org.popcraft.chunky.platform;

import org.popcraft.chunky.ChunkyBukkit;

public class BukkitPlatform implements Platform {
    private Server server;

    public BukkitPlatform(ChunkyBukkit plugin) {
        this.server = new BukkitServer(plugin);
    }

    @Override
    public Server getServer() {
        return server;
    }
}
