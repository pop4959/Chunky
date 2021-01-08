package org.popcraft.chunky.watchdog;

public abstract class AbstractGenerationWatchdog {

    public abstract boolean allowsGenerationRun();
    public abstract void stop();
    public abstract String getStopReasonKey();
    public abstract String getStartReasonKey();

}
