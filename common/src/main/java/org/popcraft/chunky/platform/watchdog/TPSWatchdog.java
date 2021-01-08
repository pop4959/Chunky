package org.popcraft.chunky.platform.watchdog;

public abstract class TPSWatchdog implements GenerationWatchdog {
    @Override
    public String getStopReasonKey() {
        return "stop_tps_low";
    }

    @Override
    public String getStartReasonKey() {
        return "start_tps_high";
    }
}
