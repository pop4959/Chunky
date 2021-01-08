package org.popcraft.chunky.platform.watchdog;

import org.popcraft.chunky.watchdog.AbstractGenerationWatchdog;

public interface Watchdogs {
    AbstractGenerationWatchdog getPlayerWatchdog();
    AbstractGenerationWatchdog getTPSWatchdog();
}
