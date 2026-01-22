package org.popcraft.chunky.platform;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.popcraft.chunky.platform.impl.AbstractNMSBatcher;

public class ForgeBatcher extends AbstractNMSBatcher {
    private final ServerLevel world;

    public ForgeBatcher(int maxWorkingCount, ServerLevel world) {
        super(maxWorkingCount);
        this.world = world;
    }

    public ForgeBatcher(ServerLevel world) {
        super();
        this.world = world;
    }

    @Override
    protected void tickTickets() {
        this.world.getChunkSource().runDistanceManagerUpdates();
    }

    @Override
    protected void executeSyncRaw(Runnable command) {
        MinecraftServer server = this.world.getServer();
        server.schedule(server.wrapRunnable(command));
    }
}
