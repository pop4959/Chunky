package org.popcraft.chunky.platform;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.popcraft.chunky.mixin.ServerChunkCacheMixin;
import org.popcraft.chunky.platform.impl.batcher.AbstractBatcher;

public class FabricBatcher extends AbstractBatcher {
    private final ServerLevel world;

    public FabricBatcher(final ServerLevel world) {
        this.world = world;
    }

    @Override
    protected void tickTickets() {
        ((ServerChunkCacheMixin) this.world.getChunkSource()).invokeRunDistanceManagerUpdates();
    }

    @Override
    protected void runSync(final Runnable command) {
        final MinecraftServer server = this.world.getServer();
        server.schedule(server.wrapRunnable(command));
    }
}
