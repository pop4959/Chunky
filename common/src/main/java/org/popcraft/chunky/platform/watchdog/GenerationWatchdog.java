package org.popcraft.chunky.platform.watchdog;

public abstract class GenerationWatchdog {

    private boolean stopped = true;

    public abstract boolean allowsGenerationRun();
    protected abstract void stopInternal();
    public abstract void startInternal();
    public abstract String getStopReasonKey();
    public abstract String getStartReasonKey();
    public abstract String getConfigName();

    public final void stop() {
        if(!stopped) {
            stopped = true;
            stopInternal();
        }
    }

    public final void start() {
        if(stopped) {
            stopped = false;
            startInternal();
        }
    }

    public final boolean isStopped() {
        return stopped;
    }
}
