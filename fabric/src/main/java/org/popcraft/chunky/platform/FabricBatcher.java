package org.popcraft.chunky.platform;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.popcraft.chunky.mixin.ServerChunkCacheMixin;
import org.popcraft.chunky.platform.impl.AbstractNMSBatcher;

public class FabricBatcher extends AbstractNMSBatcher {
    private final ServerLevel world;

    public FabricBatcher(int maxWorkingCount, ServerLevel world) {
        super(maxWorkingCount);
        this.world = world;
    }

    public FabricBatcher(ServerLevel world) {
        super();
        this.world = world;
    }

    @Override
    protected void tickTickets() {
        ((ServerChunkCacheMixin) this.world.getChunkSource()).invokeRunDistanceManagerUpdates();
    }

    @Override
    protected void executeSyncRaw(Runnable command) {
        MinecraftServer server = this.world.getServer();
        server.schedule(server.wrapRunnable(command));
    }
}
