package org.popcraft.chunky.platform;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FabricScheduler implements Scheduler {
    private ExecutorService executor;
    private final ThreadGroup tasks = new ThreadGroup("tasks");

    public FabricScheduler() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, Integer.MAX_VALUE, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        threadPoolExecutor.setThreadFactory((runnable) -> {
            Thread thread = new Thread(tasks, runnable);
            thread.setDaemon(true);
            return thread;
        });
        this.executor = threadPoolExecutor;
    }

    @Override
    public void runTaskSyncTimer(Runnable runnable, int tickInterval) {
        ServerTickEvents.StartTick handleTick = new ServerTickEvents.StartTick() {
            int tickCounter = 0;
            @Override
            public void onStartTick(MinecraftServer minecraftServer) {
                if(tickCounter % tickInterval == 0) {
                    runnable.run();
                    tickCounter = 0;
                }
                tickCounter++;
            }
        };

        ServerTickEvents.START_SERVER_TICK.register(handleTick);
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
