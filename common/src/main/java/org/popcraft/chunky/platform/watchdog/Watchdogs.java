package org.popcraft.chunky.platform.watchdog;

public interface Watchdogs {
    GenerationWatchdog getPlayerWatchdog();
    GenerationWatchdog getTPSWatchdog();
}
