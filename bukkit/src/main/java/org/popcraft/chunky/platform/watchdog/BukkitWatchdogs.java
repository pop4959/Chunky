package org.popcraft.chunky.platform.watchdog;

import org.popcraft.chunky.ChunkyBukkit;

public class BukkitWatchdogs implements Watchdogs {

    private BukkitPlayerWatchdog player;
    private BukkitTPSWatchdog tps;

    public BukkitWatchdogs(ChunkyBukkit chunky) {
        this.player = new BukkitPlayerWatchdog(chunky);
        this.tps = new BukkitTPSWatchdog(chunky);
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
