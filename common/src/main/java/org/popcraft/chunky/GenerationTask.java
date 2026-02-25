package org.popcraft.chunky;

import org.popcraft.chunky.api.event.task.GenerationCompleteEvent;
import org.popcraft.chunky.api.event.task.GenerationProgressEvent;
import org.popcraft.chunky.event.task.GenerationTaskFinishEvent;
import org.popcraft.chunky.event.task.GenerationTaskUpdateEvent;
import org.popcraft.chunky.iterator.ChunkIterator;
import org.popcraft.chunky.iterator.ChunkIteratorFactory;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.shape.Shape;
import org.popcraft.chunky.shape.ShapeFactory;
import org.popcraft.chunky.util.ChunkMath;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.RegionCache;
import org.popcraft.chunky.util.TranslationKey;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

public class GenerationTask implements Runnable {
    // Paper's async chunk system handles hundreds of concurrent requests; 200 is a good default.
    private static final int MAX_WORKING_COUNT = Input.tryInteger(System.getProperty("chunky.maxWorkingCount")).orElse(200);
    private static final long SAMPLE_INTERVAL_MS = 1000L * Math.max(Input.tryInteger(System.getProperty("chunky.sampleInterval")).orElse(30), 30);
    private static final long SAMPLE_SUB_INTERVAL_MS = SAMPLE_INTERVAL_MS / 30;
    // Fixed-size circular buffer for rate sampling — avoids allocating Pair/AtomicLong on every sub-interval.
    private static final int SAMPLE_BUFFER_SIZE = 64;
    private final Chunky chunky;
    private final Selection selection;
    private final Shape shape;
    private final AtomicLong startTime = new AtomicLong();
    private final AtomicLong updateTime = new AtomicLong();
    private final AtomicLong finishedChunks = new AtomicLong();
    // Circular buffer: timestamps and chunk counts per sub-interval bucket
    private final long[] sampleTimes = new long[SAMPLE_BUFFER_SIZE];
    private final long[] sampleCounts = new long[SAMPLE_BUFFER_SIZE];
    private int sampleHead = 0; // next write index
    private int sampleSize = 0; // valid entries
    private final AtomicBoolean updateLock = new AtomicBoolean(false); // tryLock for progress reporting
    private final Progress progress;
    private final RegionCache.WorldState worldState;
    private ChunkIterator chunkIterator;
    private volatile boolean stopped, cancelled;
    private long prevTime;

    public GenerationTask(final Chunky chunky, final Selection selection, final long count, final long time, final boolean cancelled) {
        this(chunky, selection);
        this.chunkIterator = ChunkIteratorFactory.getChunkIterator(selection, count);
        this.finishedChunks.set(count);
        this.cancelled = cancelled;
        this.prevTime = time;
    }

    public GenerationTask(final Chunky chunky, final Selection selection) {
        this.chunky = chunky;
        this.selection = selection;
        this.chunkIterator = ChunkIteratorFactory.getChunkIterator(selection);
        this.shape = ShapeFactory.getShape(selection);
        this.progress = new Progress(selection.world().getName());
        this.worldState = chunky.getRegionCache().getWorld(selection.world().getName());
    }

    private void update(final int chunkX, final int chunkZ, final boolean loaded) {
        if (stopped) {
            return;
        }
        // Increment is atomic — safe without a lock.
        final long count = finishedChunks.addAndGet(1);

        // Record generated chunk into region cache (no lock needed here — RegionCache is thread-safe).
        if (loaded) {
            worldState.setGenerated(chunkX, chunkZ);
        }

        // Only one thread at a time computes and reports progress; others skip to reduce contention.
        if (!updateLock.compareAndSet(false, true)) {
            return;
        }
        try {
            final long currentTime = System.currentTimeMillis();

            // Update circular sample buffer (lock-free since only one thread reaches this block at a time).
            if (loaded) {
                final int tail = (sampleHead == 0 ? SAMPLE_BUFFER_SIZE : sampleHead) - 1;
                if (sampleSize > 0 && currentTime - sampleTimes[tail] < SAMPLE_SUB_INTERVAL_MS) {
                    sampleCounts[tail]++;
                } else {
                    sampleTimes[sampleHead] = currentTime;
                    sampleCounts[sampleHead] = 1;
                    sampleHead = (sampleHead + 1) % SAMPLE_BUFFER_SIZE;
                    if (sampleSize < SAMPLE_BUFFER_SIZE) sampleSize++;
                }
                // Evict entries older than SAMPLE_INTERVAL_MS
                while (sampleSize > 0) {
                    final int oldestIdx = (sampleHead - sampleSize + SAMPLE_BUFFER_SIZE) % SAMPLE_BUFFER_SIZE;
                    if (currentTime - sampleTimes[oldestIdx] > SAMPLE_INTERVAL_MS) {
                        sampleSize--;
                    } else {
                        break;
                    }
                }
            }

            progress.chunkCount = count;
            progress.percentComplete = 100f * count / chunkIterator.total();
            final long chunksLeft = chunkIterator.total() - count;

            // Compute oldestTime from buffer
            final long oldestTime;
            if (sampleSize == 0) {
                oldestTime = currentTime;
            } else {
                final int oldestIdx = (sampleHead - sampleSize + SAMPLE_BUFFER_SIZE) % SAMPLE_BUFFER_SIZE;
                oldestTime = sampleTimes[oldestIdx];
            }
            final double timeDiff = (currentTime - oldestTime) / 1e3;
            if (chunksLeft > 0 && timeDiff < 1e-1) {
                return;
            }

            long sampleCount = 0;
            for (int i = 0; i < sampleSize; i++) {
                sampleCount += sampleCounts[(sampleHead - sampleSize + i + SAMPLE_BUFFER_SIZE) % SAMPLE_BUFFER_SIZE];
            }
            progress.rate = timeDiff > 0 ? sampleCount / timeDiff : 0;

            final long time;
            if (chunksLeft == 0) {
                time = (prevTime + (currentTime - startTime.get())) / 1000;
                progress.complete = true;
            } else {
                time = progress.rate > 0 ? (long) (chunksLeft / progress.rate) : Long.MAX_VALUE / 1000;
            }
            progress.hours = time / 3600;
            progress.minutes = (time - progress.hours * 3600) / 60;
            progress.seconds = time - progress.hours * 3600 - progress.minutes * 60;
            progress.chunkX = chunkX;
            progress.chunkZ = chunkZ;
            chunky.getEventBus().call(new GenerationProgressEvent(progress.world, progress.chunkCount, progress.complete, progress.percentComplete, progress.hours, progress.minutes, progress.seconds, progress.rate, progress.chunkX, progress.chunkZ));
            if (progress.complete) {
                progress.sendUpdate(chunky.getServer().getConsole());
                chunky.getEventBus().call(new GenerationTaskUpdateEvent(this));
                return;
            }
            final boolean silentMode = chunky.getConfig().isSilent();
            final boolean updateIntervalElapsed = ((currentTime - updateTime.get()) / 1e3) > chunky.getConfig().getUpdateInterval();
            if (updateIntervalElapsed) {
                if (!silentMode) {
                    progress.sendUpdate(chunky.getServer().getConsole());
                }
                chunky.getEventBus().call(new GenerationTaskUpdateEvent(this));
                updateTime.set(currentTime);
            }
        } finally {
            updateLock.set(false);
        }
    }

    @Override
    public void run() {
        final String poolThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName(String.format("Chunky-%s Thread", selection.world().getName()));
        if (!chunkIterator.process()) {
            stop(true);
        }

        // -----------------------------------------------------------------------
        // T80 Massive Async Pipeline
        // -----------------------------------------------------------------------
        // In-flight counter replaces Semaphore:
        //   acquire() → inFlight.incrementAndGet() (no kernel transition)
        //   release() → inFlight.decrementAndGet() + LockSupport.unpark()
        // LockSupport.parkNanos (user-space) is ~100ns vs. ~10µs for Semaphore.
        // -----------------------------------------------------------------------
        final AtomicInteger inFlight = new AtomicInteger(0);
        final Thread feederThread = Thread.currentThread();
        final GcHealthMonitor gcMonitor = new GcHealthMonitor();
        final boolean forceLoadExistingChunks = chunky.getConfig().isForceLoadExistingChunks();

        startTime.set(System.currentTimeMillis());

        while (!stopped && chunkIterator.hasNext()) {
            // --- Backpressure gate ---
            // Park the feeder (100 µs at a time) when:
            //   (a) the flying window is full → let completions drain before adding more
            //   (b) GC heap pressure is high  → prevent OOM by throttling submissions
            while (!stopped && (inFlight.get() >= MAX_WORKING_COUNT || gcMonitor.isStressed())) {
                LockSupport.parkNanos(feederThread, 100_000L); // 100 µs
            }
            if (stopped) break;

            // --- Zero-allocation coordinate extraction ---
            // nextLong() returns a primitive packed long — no ChunkCoordinate record allocated.
            final long chunkKey = chunkIterator.nextLong();
            final int chunkX = ChunkMath.unpackX(chunkKey);
            final int chunkZ = ChunkMath.unpackZ(chunkKey);

            final int chunkCenterX = (chunkX << 4) + 8;
            final int chunkCenterZ = (chunkZ << 4) + 8;

            if (!shape.isBounding(chunkCenterX, chunkCenterZ)) {
                update(chunkX, chunkZ, false);
                continue;
            }
            if (!forceLoadExistingChunks && worldState.isGenerated(chunkX, chunkZ)) {
                update(chunkX, chunkZ, false);
                continue;
            }

            // Claim a slot in the flying window before submitting the async chain.
            inFlight.incrementAndGet();

            final CompletableFuture<Boolean> isChunkGenerated = forceLoadExistingChunks
                    ? CompletableFuture.completedFuture(false)
                    : selection.world().isChunkGenerated(chunkX, chunkZ);

            isChunkGenerated
                    .thenCompose(generated -> Boolean.TRUE.equals(generated)
                            ? CompletableFuture.completedFuture(null)
                            : selection.world().getChunkAtAsync(chunkX, chunkZ))
                    .whenComplete((ignored, throwable) -> {
                        // Release the flying-window slot and immediately wake the feeder
                        // so it can submit the next chunk without waiting for a timer.
                        inFlight.decrementAndGet();
                        LockSupport.unpark(feederThread);
                        update(chunkX, chunkZ, true);
                    });
        }

        // --- Drain: wait for all in-flight futures to settle before finishing ---
        // Park 1 ms at a time so we don't spin-burn CPU; completions will unpark us.
        while (inFlight.get() > 0 && !stopped) {
            LockSupport.parkNanos(feederThread, 1_000_000L); // 1 ms
        }

        if (stopped) {
            chunky.getServer().getConsole().sendMessagePrefixed(TranslationKey.TASK_STOPPED, selection.world().getName());
        } else {
            cancelled = true;
        }
        chunky.getTaskLoader().saveTask(this);
        chunky.getGenerationTasks().remove(selection.world().getName());
        Thread.currentThread().setName(poolThreadName);
        chunky.getEventBus().call(new GenerationTaskFinishEvent(this));
        chunky.getEventBus().call(new GenerationCompleteEvent(selection.world().getName()));
    }

    // -----------------------------------------------------------------------
    // GC Health Monitor — Backpressure Controller
    // -----------------------------------------------------------------------
    /**
     * Samples JVM heap usage to implement intelligent backpressure.
     * When heapUsed / heapMax > PRESSURE_THRESHOLD the pipeline pauses injecting new
     * async chunk requests, preventing cascading GC pauses and OOM crashes.
     *
     * <p>Heap is only sampled every {@code CHECK_INTERVAL_NS} nanoseconds to avoid
     * making an expensive {@link MemoryMXBean} call on every dispatch iteration.
     */
    private static final class GcHealthMonitor {
        private static final double PRESSURE_THRESHOLD = 0.85; // pause at 85 % heap used
        private static final long CHECK_INTERVAL_NS = 200_000_000L; // re-check every 200 ms
        private static final MemoryMXBean MEMORY_BEAN = ManagementFactory.getMemoryMXBean();

        private volatile boolean stressed = false;
        private long lastCheckNs = 0L;

        /**
         * Returns {@code true} if the JVM heap is under pressure and the feeder should
         * pause submitting new chunk requests until memory is freed.
         */
        boolean isStressed() {
            final long now = System.nanoTime();
            if (now - lastCheckNs >= CHECK_INTERVAL_NS) {
                lastCheckNs = now;
                final java.lang.management.MemoryUsage usage = MEMORY_BEAN.getHeapMemoryUsage();
                final long used = usage.getUsed();
                final long max  = usage.getMax();
                // max can be -1 if unbounded; treat as not stressed in that case.
                stressed = max > 0 && (double) used / max > PRESSURE_THRESHOLD;
            }
            return stressed;
        }
    }

    public void stop(final boolean cancelled) {
        this.stopped = true;
        this.cancelled = cancelled;
    }

    public Chunky getChunky() {
        return chunky;
    }

    public Selection getSelection() {
        return selection;
    }

    public long getCount() {
        return finishedChunks.get();
    }

    public ChunkIterator getChunkIterator() {
        return chunkIterator;
    }

    public Shape getShape() {
        return shape;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public long getTotalTime() {
        return prevTime + (startTime.get() > 0 ? System.currentTimeMillis() - startTime.get() : 0);
    }

    public Progress getProgress() {
        return progress;
    }

    @SuppressWarnings("unused")
    public static final class Progress {
        private final String world;
        private long chunkCount;
        private boolean complete;
        private float percentComplete;
        private long hours, minutes, seconds;
        private double rate;
        private int chunkX, chunkZ;

        private Progress(final String world) {
            this.world = world;
        }

        public String getWorld() {
            return world;
        }

        public long getChunkCount() {
            return chunkCount;
        }

        public boolean isComplete() {
            return complete;
        }

        public float getPercentComplete() {
            return percentComplete;
        }

        public long getHours() {
            return hours;
        }

        public long getMinutes() {
            return minutes;
        }

        public long getSeconds() {
            return seconds;
        }

        public double getRate() {
            return rate;
        }

        public int getChunkX() {
            return chunkX;
        }

        public int getChunkZ() {
            return chunkZ;
        }

        public void sendUpdate(final Sender sender) {
            if (complete) {
                sender.sendMessagePrefixed(TranslationKey.TASK_DONE, world, chunkCount, String.format("%.2f", percentComplete), String.format("%01d", hours), String.format("%02d", minutes), String.format("%02d", seconds));
            } else {
                sender.sendMessagePrefixed(TranslationKey.TASK_UPDATE, world, chunkCount, String.format("%.2f", percentComplete), String.format("%01d", hours), String.format("%02d", minutes), String.format("%02d", seconds), String.format("%.1f", rate), chunkX, chunkZ);
            }
        }
    }
}
