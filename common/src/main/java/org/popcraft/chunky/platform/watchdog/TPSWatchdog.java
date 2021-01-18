package org.popcraft.chunky.platform.watchdog;

import org.popcraft.chunky.Chunky;

public abstract class TPSWatchdog implements GenerationWatchdog {

    protected int configuredTPS;

    public TPSWatchdog(Chunky chunky) {
        this.configuredTPS = chunky.getConfig().getWatchdogStartOn("tps");
    }

    @Override
    public String getStopReasonKey() {
        return "stop_tps_low";
    }

    @Override
    public String getStartReasonKey() {
        return "start_tps_high";
    }
}
