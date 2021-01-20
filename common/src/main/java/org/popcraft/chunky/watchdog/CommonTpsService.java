package org.popcraft.chunky.watchdog;

import java.util.Arrays;
import java.util.OptionalDouble;
import java.util.concurrent.locks.ReentrantLock;

public class CommonTpsService {
    private int TICK_COUNT;
    private long[] TICK_TIMES = new long[100];
    private long lastTick = -1;
    private ReentrantLock lock = new ReentrantLock();

    public void saveTickTime() {
        if(lastTick == -1) {
            lastTick = System.nanoTime() - 50_000_000; //50_000_000 = 1/20th of a second in nanoseconds
        }
        TICK_TIMES[TICK_COUNT % TICK_TIMES.length] = System.nanoTime() - lastTick;
        lastTick = System.nanoTime();

        TICK_COUNT += 1;
    }

    public double getTPS() {
        double tps = 20;
        lock.lock();
        OptionalDouble averageTickTime = Arrays.stream(TICK_TIMES).average();
        lock.unlock();
        if(averageTickTime.isPresent()) {
            double averageSec = averageTickTime.getAsDouble() / 1_000_000_000D; //Convert to seconds
            tps = 1D / averageSec;
        }
        return tps;
    }
}
