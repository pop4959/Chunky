package org.popcraft.chunky;

import org.popcraft.chunky.iterator.ChunkIterator;
import org.popcraft.chunky.iterator.ChunkIteratorFactory;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.shape.Shape;
import org.popcraft.chunky.shape.ShapeFactory;
import org.popcraft.chunky.util.ChunkCoordinate;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

public class GenerationTask implements Runnable {
    private final Chunky chunky;
    private final Selection selection;
    private ChunkIterator chunkIterator;
    private final Shape shape;
    private boolean stopped, cancelled;
    private long prevTime;
    private final AtomicLong startTime = new AtomicLong();
    private final AtomicLong printTime = new AtomicLong();
    private final AtomicLong finishedChunks = new AtomicLong();
    private final AtomicLong totalChunks = new AtomicLong();
    private final ConcurrentLinkedQueue<Long> chunkUpdateTimes = new ConcurrentLinkedQueue<>();
    private static final int MAX_WORKING = 50;
    private final Progress progress;

    public GenerationTask(Chunky chunky, Selection selection, long count, long time) {
        this(chunky, selection);
        this.chunkIterator = ChunkIteratorFactory.getChunkIterator(selection, count);
        this.finishedChunks.set(count);
        this.prevTime = time;
    }

    public GenerationTask(Chunky chunky, Selection selection) {
        this.chunky = chunky;
        this.selection = selection;
        this.chunkIterator = ChunkIteratorFactory.getChunkIterator(selection);
        this.shape = ShapeFactory.getShape(selection);
        this.totalChunks.set(chunkIterator.total());
        this.progress = new Progress(selection.world().getName());
    }

    @SuppressWarnings("ConstantConditions")
    private void printUpdate(int chunkX, int chunkZ) {
        if (stopped) {
            return;
        }
        this.progress.chunkCount = finishedChunks.addAndGet(1);
        this.progress.percentComplete = 100f * this.progress.chunkCount / totalChunks.get();
        final long currentTime = System.currentTimeMillis();
        chunkUpdateTimes.add(currentTime);
        while (currentTime - chunkUpdateTimes.peek() > 1e4) {
            chunkUpdateTimes.poll();
        }
        final long chunksLeft = totalChunks.get() - finishedChunks.get();
        if (chunksLeft > 0 && (chunky.getOptions().isSilent() || ((currentTime - printTime.get()) / 1e3) < chunky.getOptions().getQuietInterval())) {
            return;
        }
        printTime.set(currentTime);
        final long oldestTime = chunkUpdateTimes.peek();
        final double timeDiff = (currentTime - oldestTime) / 1e3;
        if (chunksLeft > 0 && timeDiff < 1e-1) {
            return;
        }
        this.progress.rate = chunkUpdateTimes.size() / timeDiff;
        final long time;
        if (chunksLeft == 0) {
            time = (prevTime + (currentTime - startTime.get())) / 1000;
            this.progress.complete = true;
        } else {
            time = (long) (chunksLeft / this.progress.rate);
        }
        this.progress.hours = time / 3600;
        this.progress.minutes = (time - this.progress.hours * 3600) / 60;
        this.progress.seconds = time - this.progress.hours * 3600 - this.progress.minutes * 60;
        this.progress.chunkX = chunkX;
        this.progress.chunkZ = chunkZ;
        this.progress.sendUpdate(chunky.getServer().getConsoleSender());
    }

    @Override
    public void run() {
        final String poolThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName(String.format("Chunky-%s Thread", selection.world().getName()));
        final Semaphore working = new Semaphore(MAX_WORKING);
        startTime.set(System.currentTimeMillis());
        while (!stopped && chunkIterator.hasNext()) {
            final ChunkCoordinate chunkCoord = chunkIterator.next();
            final int chunkCenterX = (chunkCoord.x << 4) + 8;
            final int chunkCenterZ = (chunkCoord.z << 4) + 8;
            if (!shape.isBounding(chunkCenterX, chunkCenterZ) || selection.world().isChunkGenerated(chunkCoord.x, chunkCoord.z)) {
                printUpdate(chunkCoord.x, chunkCoord.z);
                continue;
            }
            try {
                working.acquire();
            } catch (InterruptedException e) {
                stop(cancelled);
                break;
            }
            selection.world().getChunkAtAsync(chunkCoord.x, chunkCoord.z).thenRun(() -> {
                working.release();
                printUpdate(chunkCoord.x, chunkCoord.z);
            });
        }
        if (stopped) {
            chunky.getServer().getConsoleSender().sendMessagePrefixed("task_stopped", selection.world().getName());
        } else {
            this.cancelled = true;
        }
        chunky.getConfig().saveTask(this);
        chunky.getGenerationTasks().remove(selection.world());
        Thread.currentThread().setName(poolThreadName);
    }

    public void stop(final boolean cancelled) {
        this.stopped = true;
        this.cancelled = cancelled;
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

    public static class Progress {
        private final String world;
        private long chunkCount;
        private boolean complete;
        private double percentComplete;
        private long hours, minutes, seconds;
        private double rate;
        private int chunkX, chunkZ;

        private Progress(final String world) {
            this.world = world;
        }

        private void sendUpdate(final Sender sender) {
            if (this.complete) {
                sender.sendMessagePrefixed("task_done", world, chunkCount, String.format("%.2f", percentComplete), String.format("%01d", hours), String.format("%02d", minutes), String.format("%02d", seconds));
            } else {
                sender.sendMessagePrefixed("task_update", world, chunkCount, String.format("%.2f", percentComplete), String.format("%01d", hours), String.format("%02d", minutes), String.format("%02d", seconds), String.format("%.1f", rate), chunkX, chunkZ);
            }
        }
    }
}
