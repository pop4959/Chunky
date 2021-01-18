package org.popcraft.chunky.platform.watchdog;

import org.popcraft.chunky.ChunkySponge;

public class SpongeWatchdogs implements Watchdogs {

    private SpongePlayerWatchdog player;
    private SpongeTPSWatchdog tps;

    public SpongeWatchdogs(ChunkySponge chunky) {
        this.player = new SpongePlayerWatchdog(chunky);
        this.tps = new SpongeTPSWatchdog();
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
