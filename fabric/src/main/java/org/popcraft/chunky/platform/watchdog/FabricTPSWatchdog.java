package org.popcraft.chunky.platform.watchdog;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.popcraft.chunky.ChunkyFabric;
import org.popcraft.chunky.watchdog.CommonTpsService;

public class FabricTPSWatchdog extends TPSWatchdog {

    private CommonTpsService tpsService;

    public FabricTPSWatchdog(ChunkyFabric chunky, CommonTpsService tpsService) {
        super(chunky.getChunky());
        this.tpsService = tpsService;
        ServerTickEvents.START_SERVER_TICK.register(s -> {
            tpsService.saveTickTime();
        });
    }

    @Override
    public boolean allowsGenerationRun() {
        return tpsService.getTPS() >= super.configuredTPS;
    }

    @Override
    public void stop() {
        //TODO: How to cancel event listener?
    }
}
