package org.popcraft.chunky.platform.watchdog;

public class SpongeTPSWatchdog extends TPSWatchdog {
    @Override
    public boolean allowsGenerationRun() {
        return false;
    }

    @Override
    public void stop() {

    }
}
