package org.popcraft.chunky.platform;

import org.popcraft.chunky.ChunkySponge;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

public class SpongeScheduler implements Scheduler {
    private ChunkySponge plugin;

    public SpongeScheduler(ChunkySponge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runTaskSyncTimer(Runnable runnable, int tickInterval) {
        Sponge.getScheduler().createTaskBuilder().intervalTicks(tickInterval).execute(runnable).submit(plugin);
    }

    @Override
    public void runTaskAsync(Runnable runnable) {
        Sponge.getScheduler().createTaskBuilder().async().execute(runnable).submit(plugin);
    }

    @Override
    public void cancelTasks() {
        Sponge.getScheduler().getScheduledTasks(plugin).forEach(Task::cancel);
    }
}
