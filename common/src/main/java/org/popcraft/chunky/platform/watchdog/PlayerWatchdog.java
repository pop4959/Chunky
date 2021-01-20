package org.popcraft.chunky.platform.watchdog;

import org.popcraft.chunky.Chunky;

public abstract class PlayerWatchdog implements GenerationWatchdog {

    private Chunky chunky;

    public PlayerWatchdog(Chunky chunky) {
        this.chunky = chunky;
    }

    @Override
    public String getStopReasonKey() {
        return "stop_player_online";
    }

    @Override
    public String getStartReasonKey() {
        return "start_no_players";
    }

    protected int getConfiguredPlayerCount() {
        return this.chunky.getConfig().getWatchdogStartOn("players");
    }
}
