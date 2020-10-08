package org.popcraft.chunky.task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BukkitTaskManager implements TaskManager {
    public final ThreadPoolExecutor executor = new ThreadPoolExecutor(3, Integer.MAX_VALUE, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().build());
    private final Map<World, GenerationTask> tasks = new ConcurrentHashMap<>();

    public BukkitTaskManager() {
        executor.prestartAllCoreThreads();
        executor.allowCoreThreadTimeOut(true);
    }

    @Override
    public void start(World world, GenerationTask task) {
        executor.execute(task);
        tasks.put(world, task);
    }

    @Override
    public boolean isRunning(World world) {
        return tasks.containsKey(world);
    }

    @Override
    public List<GenerationTask> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void stop(World world, GenerationTask task, boolean cancelled, boolean save) {
        task.stop(cancelled, save);
        tasks.remove(world);
    }

    @Override
    public void stopAll(boolean shutdown, boolean cancelled, boolean save) {
        for (World world : tasks.keySet()) {
            stop(world, tasks.get(world), cancelled, save);
        }
        tasks.clear();
        if (shutdown) {
            this.executor.shutdownNow();
        }
    }
}
