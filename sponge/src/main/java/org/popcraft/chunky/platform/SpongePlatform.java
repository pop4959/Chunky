package org.popcraft.chunky.platform;

import org.popcraft.chunky.ChunkySponge;

public class SpongePlatform implements Platform {
    private Server server;

    public SpongePlatform(ChunkySponge plugin) {
        this.server = new SpongeServer(plugin);
    }

    @Override
    public Server getServer() {
        return server;
    }
}
