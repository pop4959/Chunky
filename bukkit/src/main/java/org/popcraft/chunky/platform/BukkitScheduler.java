package org.popcraft.chunky.platform;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.popcraft.chunky.ChunkyBukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

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
    public void cancelAllTasks() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }

    @Override
    public void cancelAsyncTasks() {
        Bukkit.getScheduler().getPendingTasks().forEach(bukkitTask -> {
            if(bukkitTask.getOwner() == plugin && !bukkitTask.isSync()) {
                bukkitTask.cancel();
            }
        });
    }
}
