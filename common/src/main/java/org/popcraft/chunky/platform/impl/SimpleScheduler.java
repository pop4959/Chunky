package org.popcraft.chunky.platform.impl;

import org.popcraft.chunky.platform.Scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SimpleScheduler implements Scheduler {
    private final ExecutorService executor;
    private final ThreadGroup tasks = new ThreadGroup("tasks");

    public SimpleScheduler() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, Integer.MAX_VALUE, 5, TimeUnit.MINUTES, new SynchronousQueue<>());
        threadPoolExecutor.setThreadFactory(runnable -> {
            Thread thread = new Thread(tasks, runnable);
            thread.setDaemon(true);
            return thread;
        });
        threadPoolExecutor.prestartAllCoreThreads();
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        this.executor = threadPoolExecutor;
    }

    @Override
    public void runTaskAsync(Runnable runnable) {
        executor.submit(runnable);
    }

    @Override
    public void cancelTasks() {
        tasks.interrupt();
    }
}
