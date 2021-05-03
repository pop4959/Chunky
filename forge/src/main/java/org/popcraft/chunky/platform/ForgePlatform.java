package org.popcraft.chunky.platform;

import net.minecraft.server.MinecraftServer;
import org.popcraft.chunky.ChunkyForge;

public class ForgePlatform implements Platform {
    private Server server;

    public ForgePlatform(ChunkyForge plugin, MinecraftServer server) {
        this.server = new ForgeServer(plugin, server);
    }

    @Override
    public Server getServer() {
        return server;
    }
}
