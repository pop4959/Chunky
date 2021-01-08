package org.popcraft.chunky.platform.watchdog;

import org.popcraft.chunky.ChunkyBukkit;
import org.popcraft.chunky.watchdog.AbstractGenerationWatchdog;

public class BukkitWatchdogs implements Watchdogs {

    private BukkitPlayerWatchdog player;
    private BukkitTPSWatchdog tps;

    public BukkitWatchdogs(ChunkyBukkit chunky) {
        this.player = new BukkitPlayerWatchdog(chunky);
        this.tps = new BukkitTPSWatchdog(chunky);
    }

    @Override
    public AbstractGenerationWatchdog getPlayerWatchdog() {
        return player;
    }

    @Override
    public AbstractGenerationWatchdog getTPSWatchdog() {
        return tps;
    }
}
