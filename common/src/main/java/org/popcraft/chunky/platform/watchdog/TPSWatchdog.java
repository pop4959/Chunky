package org.popcraft.chunky.platform.watchdog;

import org.popcraft.chunky.Chunky;

public abstract class TPSWatchdog implements GenerationWatchdog {

    private Chunky chunky;

    public TPSWatchdog(Chunky chunky) {
        this.chunky = chunky;
    }

    @Override
    public String getStopReasonKey() {
        return "stop_tps_low";
    }

    @Override
    public String getStartReasonKey() {
        return "start_tps_high";
    }

    protected int getConfiguredTPS() {
        return this.chunky.getConfig().getWatchdogStartOn("tps");
    }
}
