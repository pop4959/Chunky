package org.popcraft.chunky.platform.watchdog;

public abstract class PlayerWatchdog implements GenerationWatchdog {
    @Override
    public String getStopReasonKey() {
        return "stop_player_online";
    }

    @Override
    public String getStartReasonKey() {
        return "start_no_players";
    }
}
