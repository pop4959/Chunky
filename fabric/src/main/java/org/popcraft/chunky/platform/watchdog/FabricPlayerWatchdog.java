package org.popcraft.chunky.platform.watchdog;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.popcraft.chunky.ChunkyFabric;
import java.util.concurrent.atomic.AtomicInteger;

public class FabricPlayerWatchdog extends PlayerWatchdog {

    private AtomicInteger playerCount;

    public FabricPlayerWatchdog(ChunkyFabric chunky) {
        super(chunky.getChunky());
        this.playerCount = new AtomicInteger();
        ServerTickEvents.START_SERVER_TICK.register(t -> {
            //This isn't too expensive so it's probably fine to do every tick
            playerCount.set(t.getCurrentPlayerCount());
        });
    }

    @Override
    public boolean allowsGenerationRun() {
        return super.configuredPlayerCount >= playerCount.get();
    }

    @Override
    public void stop() {
        //TODO: How to cancel the event listener?
    }
}
