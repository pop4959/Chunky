package org.popcraft.chunky.task;

import org.bukkit.World;

import java.util.List;

public interface TaskManager {
    void start(World world, GenerationTask task);

    boolean isRunning(World world);

    List<GenerationTask> getTasks();

    void stop(World world, GenerationTask task, boolean cancelled, boolean save);

    void stopAll(boolean shutdown, boolean cancelled, boolean save);
}
