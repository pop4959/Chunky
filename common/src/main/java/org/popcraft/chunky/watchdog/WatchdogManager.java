package org.popcraft.chunky.watchdog;

import org.popcraft.chunky.platform.watchdog.GenerationWatchdog;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WatchdogManager {

    private List<GenerationWatchdog> watchdogs = new ArrayList<>();

    public Optional<GenerationWatchdog> getUnmetWatchdog() {
        GenerationWatchdog unmet = null;
        for (GenerationWatchdog watchdog : watchdogs) {
            if(!watchdog.allowsGenerationRun()) {
                unmet = watchdog;
                break;
            }
        }
        return Optional.ofNullable(unmet);
    }

    public void registerWatchdog(GenerationWatchdog watchdog) {
        watchdogs.add(watchdog);
    }
    public void stopAll() {
        watchdogs.forEach(GenerationWatchdog::stop);
    }
}