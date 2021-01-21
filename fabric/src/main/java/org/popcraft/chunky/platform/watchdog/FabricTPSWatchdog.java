package org.popcraft.chunky.platform.watchdog;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.popcraft.chunky.ChunkyFabric;
import org.popcraft.chunky.watchdog.CommonTpsService;

public class FabricTPSWatchdog extends TPSWatchdog {

    private CommonTpsService tpsService;
    private ChunkyFabric chunky;
    private boolean registeredHandler = false;

    public FabricTPSWatchdog(ChunkyFabric chunky, CommonTpsService tpsService) {
        this.chunky = chunky;
        this.tpsService = tpsService;
    }

    @Override
    public boolean allowsGenerationRun() {
        return tpsService.getTPS() >= chunky.getChunky().getConfig().getWatchdogStartOn("tps");
    }

    @Override
    public void stopInternal() {
        //TODO: How to cancel event listener?
    }

    @Override
    public void startInternal() {
        //We have to do this because we're not actually stopping anything, meaning the GenerationWatchdog logic wouldn't work.
        if (!registeredHandler) {
            ServerTickEvents.START_SERVER_TICK.register(s -> tpsService.saveTickTime());
            registeredHandler = true;
        }
    }
}
