package org.popcraft.chunky.platform.watchdog;

import org.bukkit.scheduler.BukkitTask;
import org.popcraft.chunky.ChunkyBukkit;
import org.popcraft.chunky.watchdog.AbstractGenerationWatchdog;

import java.util.Arrays;
import java.util.OptionalDouble;

public class BukkitTPSWatchdog extends AbstractGenerationWatchdog {
    private int TICK_COUNT;
    private long[] TICK_TIMES = new long[100];
    private long lastTick = -1;
    private BukkitTask task;
    private ChunkyBukkit chunky;

    public BukkitTPSWatchdog(ChunkyBukkit chunky) {
        this.chunky = chunky;
        task = chunky.getServer().getScheduler().runTaskTimer(chunky, this::saveTickTime, 0, 1);
    }

    @Override
    public boolean allowsGenerationRun() {
        return this.getTPS() >= this.chunky.getConfig().getDouble("watchdogs.tps.start-on");
    }

    @Override
    public void stop() {
        task.cancel();
    }

    @Override
    public String getStopReasonKey() {
        return "stop_tps_low";
    }

    @Override
    public String getStartReasonKey() {
        return "start_tps_high";
    }

    private void saveTickTime() {
        if(lastTick == -1) {
            lastTick = System.nanoTime() - 50_000_000; //50_000_000 = 1/20th of a second in nanoseconds
        }
        TICK_TIMES[TICK_COUNT % TICK_TIMES.length] = System.nanoTime() - lastTick;
        lastTick = System.nanoTime();

        TICK_COUNT += 1;
    }

    private double getTPS() {
        double tps = 20;
        OptionalDouble averageTickTime = Arrays.stream(TICK_TIMES).average();
        if(averageTickTime.isPresent()) {
            double averageSec = averageTickTime.getAsDouble() / 1_000_000_000D;
            tps = 1D / averageSec;
        }
        return tps;
    }
}
