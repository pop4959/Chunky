package org.popcraft.chunky.platform;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.popcraft.chunky.platform.impl.batcher.AbstractBatcher;

public class NeoForgeBatcher extends AbstractBatcher {
    private final ServerLevel world;

    public NeoForgeBatcher(final ServerLevel world) {
        this.world = world;
    }

    @Override
    protected void tickTickets() {
        this.world.getChunkSource().runDistanceManagerUpdates();
    }

    @Override
    protected void runSync(final Runnable command) {
        final MinecraftServer server = this.world.getServer();
        server.schedule(server.wrapRunnable(command));
    }
}
