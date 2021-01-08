package org.popcraft.chunky.platform.watchdog;

public interface GenerationWatchdog {

    public boolean allowsGenerationRun();
    public void stop();
    public String getStopReasonKey();
    public String getStartReasonKey();

}
