package org.popcraft.chunky.platform;

import org.bukkit.Bukkit;
import org.popcraft.chunky.ChunkyBukkit;

public class BukkitScheduler implements Scheduler {
    private ChunkyBukkit plugin;

    public BukkitScheduler(ChunkyBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runTaskSyncTimer(Runnable runnable, int tickInterval) {
        Bukkit.getScheduler().runTaskTimer(plugin, runnable, 0, tickInterval);
    }

    @Override
    public void runTaskAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    @Override
    public void cancelTasks() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }
}
