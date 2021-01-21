package org.popcraft.chunky.platform.watchdog;

import org.popcraft.chunky.Chunky;

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
