package org.popcraft.chunky.platform;

public interface Scheduler {
    void runTaskAsync(Runnable runnable);

    void cancelTasks();
}
