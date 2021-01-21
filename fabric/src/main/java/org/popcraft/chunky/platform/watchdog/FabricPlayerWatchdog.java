package org.popcraft.chunky.platform.watchdog;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.ChunkyFabric;

import java.util.concurrent.atomic.AtomicInteger;

public class FabricPlayerWatchdog extends PlayerWatchdog {

    private AtomicInteger playerCount;
    private ChunkyFabric chunky;
    private boolean registeredHandler = false;

    public FabricPlayerWatchdog(ChunkyFabric chunky) {
        this.chunky = chunky;
        this.playerCount = new AtomicInteger();
    }

    @Override
    public boolean allowsGenerationRun() {
        return this.chunky.getChunky().getConfig().getWatchdogStartOn("players") >= playerCount.get();
    }

    @Override
    public void stopInternal() {
        //TODO: How to cancel the event listener? Upon further investigation, this may not be possible...
    }

    @Override
    public void startInternal() {
        //We have to do this because we're not actually stopping anything, meaning the GenerationWatchdog logic wouldn't work.
        if (!registeredHandler) {
            ServerTickEvents.START_SERVER_TICK.register(t -> {
                //This isn't too expensive so it's probably fine to do every tick
                playerCount.set(t.getCurrentPlayerCount());
            });
            registeredHandler = true;
        }
    }
}
