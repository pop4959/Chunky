package org.popcraft.chunky.platform.watchdog;

import org.popcraft.chunky.ChunkyFabric;
import org.popcraft.chunky.watchdog.CommonTpsService;

public class FabricWatchdogs implements Watchdogs {

    private FabricPlayerWatchdog player;
    private FabricTPSWatchdog tps;

    public FabricWatchdogs(ChunkyFabric chunky) {
        this.player = new FabricPlayerWatchdog(chunky);
        this.tps = new FabricTPSWatchdog(chunky, new CommonTpsService());
    }

    @Override
    public GenerationWatchdog getPlayerWatchdog() {
        return player;
    }

    @Override
    public GenerationWatchdog getTPSWatchdog() {
        return tps;
    }
}
