package org.popcraft.chunky.watchdog;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WatchdogManager {

    private List<AbstractGenerationWatchdog> watchdogs = new ArrayList<>();

    public Optional<AbstractGenerationWatchdog> getUnmetWatchdog() {
        AbstractGenerationWatchdog unmet = null;
        for (AbstractGenerationWatchdog watchdog : watchdogs) {
            if(!watchdog.allowsGenerationRun()) {
                unmet = watchdog;
                break;
            }
        }
        return Optional.ofNullable(unmet);
    }

    public void registerWatchdog(AbstractGenerationWatchdog watchdog) {
        watchdogs.add(watchdog);
    }
    public void stopAll() {
        watchdogs.forEach(AbstractGenerationWatchdog::stop);
    }
}