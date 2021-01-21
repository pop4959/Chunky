package org.popcraft.chunky.platform.watchdog;

public abstract class TPSWatchdog extends GenerationWatchdog {

    @Override
    public String getStopReasonKey() {
        return "stop_tps_low";
    }

    @Override
    public String getStartReasonKey() {
        return "start_tps_high";
    }

    @Override
    public String getConfigName() {
        return "tps";
    }
}
