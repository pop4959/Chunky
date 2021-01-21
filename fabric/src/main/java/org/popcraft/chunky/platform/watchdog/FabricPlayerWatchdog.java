package org.popcraft.chunky.platform.watchdog;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.ChunkyFabric;
import java.util.concurrent.atomic.AtomicInteger;

public class FabricPlayerWatchdog extends PlayerWatchdog {

    private AtomicInteger playerCount;
    private ChunkyFabric chunky;

    public FabricPlayerWatchdog(ChunkyFabric chunky) {
        this.chunky = chunky;
        this.playerCount = new AtomicInteger();
        ServerTickEvents.START_SERVER_TICK.register(t -> {
            //This isn't too expensive so it's probably fine to do every tick
            playerCount.set(t.getCurrentPlayerCount());
        });
    }

    @Override
    public boolean allowsGenerationRun() {
        return this.chunky.getChunky().getConfig().getWatchdogStartOn("players") >= playerCount.get();
    }

    @Override
    public void stop() {
        //TODO: How to cancel the event listener?
    }
}
