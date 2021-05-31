package org.popcraft.chunky.platform;

import org.popcraft.chunky.ChunkySponge;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.ScheduledTask;

public class SpongeScheduler implements Scheduler {
    private ChunkySponge plugin;

    public SpongeScheduler(ChunkySponge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runTaskAsync(Runnable runnable) {
        Sponge.asyncScheduler().createExecutor(plugin.getContainer()).execute(runnable);
    }

    @Override
    public void cancelTasks() {
        Sponge.asyncScheduler().tasks(plugin.getContainer()).forEach(ScheduledTask::cancel);
    }
}
