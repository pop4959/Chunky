package org.popcraft.chunky.platform;

import net.minecraft.server.MinecraftServer;
import org.popcraft.chunky.ChunkyFabric;

public class FabricPlatform implements Platform {
    private Server server;

    public FabricPlatform(ChunkyFabric plugin, MinecraftServer minecraftServer) {
        this.server = new FabricServer(plugin, minecraftServer);
    }

    @Override
    public Server getServer() {
        return server;
    }
}
