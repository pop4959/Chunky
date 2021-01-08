package org.popcraft.chunky.platform.watchdog;

import org.popcraft.chunky.ChunkyFabric;

public class FabricWatchdogs implements Watchdogs {

    private FabricPlayerWatchdog player;
    private FabricTPSWatchdog tps;

    public FabricWatchdogs(ChunkyFabric chunky) {
        this.player = new FabricPlayerWatchdog(chunky);
        this.tps = new FabricTPSWatchdog(chunky);
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
