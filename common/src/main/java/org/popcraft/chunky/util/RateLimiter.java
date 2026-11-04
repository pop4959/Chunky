package org.popcraft.chunky.util;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class RateLimiter {
    private static final long SLOT_MILLIS = 100;
    private static final long SECOND_NANOS = TimeUnit.SECONDS.toNanos(1);
    private static final long BUDGET_POLL_NANOS = TimeUnit.MILLISECONDS.toNanos(10);
    private final long windowMillis;
    private final long[] slotIndexes;
    private final long[] slotCounts;
    private volatile int limit;
    private boolean restart = true;
    private long window, start, next;

    public RateLimiter(final long windowMillis, final int limit) {
        final int slots = (int) (windowMillis / SLOT_MILLIS) + 1;
        this.windowMillis = (slots - 1) * SLOT_MILLIS;
        this.slotIndexes = new long[slots];
        this.slotCounts = new long[slots];
        this.limit = Math.max(0, limit);
    }

    public int getLimit() {
        return limit;
    }

    public synchronized void setLimit(final int limit) {
        final int newLimit = Math.max(0, limit);
        if (newLimit != this.limit) {
            this.limit = newLimit;
            this.restart = true;
        }
    }

    public long acquire(final Semaphore working, final int maxWorkingCount) {
        while (limit > 0 && !Thread.currentThread().isInterrupted()) {
            final long delay;
            synchronized (this) {
                delay = delay(System.currentTimeMillis(), System.nanoTime(), maxWorkingCount - working.availablePermits());
                if (delay == 0) {
                    return window;
                }
            }
            LockSupport.parkNanos(delay);
        }
        return 0;
    }

    synchronized long delay(final long nowMillis, final long nowNanos, final long working) {
        if (limit <= 0) {
            return 0;
        }
        final long interval = (SECOND_NANOS + limit - 1) / limit;
        if (restart) {
            restart = false;
            ++window;
            Arrays.fill(slotCounts, 0);
            start = nowMillis;
            next = nowNanos + interval;
        }
        final long allowance = limit * Math.min(windowMillis, nowMillis - start) / 1000;
        if (count(nowMillis, slotIndexes.length) + working > allowance) {
            return Math.min(interval, BUDGET_POLL_NANOS);
        }
        if (nowNanos - next < 0) {
            return next - nowNanos;
        }
        next = Math.max(next, nowNanos - interval) + interval;
        return 0;
    }

    public void release(final long window) {
        if (window > 0) {
            record(System.currentTimeMillis(), window);
        }
    }

    synchronized void record(final long nowMillis, final long window) {
        if (window != this.window) {
            return;
        }
        final long index = nowMillis / SLOT_MILLIS;
        final int slot = (int) (index % slotIndexes.length);
        if (slotIndexes[slot] != index) {
            slotIndexes[slot] = index;
            slotCounts[slot] = 0;
        }
        ++slotCounts[slot];
    }

    public synchronized double rate(final long nowMillis) {
        final long millis = Math.min(windowMillis, nowMillis - start);
        return limit > 0 && !restart && millis > 0 ? 1000d * count(nowMillis, slotIndexes.length - 1) / millis : 0;
    }

    private long count(final long nowMillis, final int slots) {
        final long index = nowMillis / SLOT_MILLIS;
        long count = 0;
        for (int slot = 0; slot < slotIndexes.length; ++slot) {
            if (slotIndexes[slot] > index - slots) {
                count += slotCounts[slot];
            }
        }
        return count;
    }
}
