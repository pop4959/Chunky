package org.popcraft.chunky.platform;

public interface Scheduler {
    void runTaskSyncTimer(Runnable runnable, int tickInterval);

    void runTaskAsync(Runnable runnable);

    void cancelTasks();
}
