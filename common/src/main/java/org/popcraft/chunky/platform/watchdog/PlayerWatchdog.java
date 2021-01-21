package org.popcraft.chunky.platform.watchdog;

import org.popcraft.chunky.Chunky;

public abstract class PlayerWatchdog extends GenerationWatchdog {

    @Override
    public String getStopReasonKey() {
        return "stop_player_online";
    }

    @Override
    public String getStartReasonKey() {
        return "start_no_players";
    }

    @Override
    public String getConfigName() {
        return "players";
    }
}
