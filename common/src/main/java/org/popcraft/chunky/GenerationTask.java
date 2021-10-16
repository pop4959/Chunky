package org.popcraft.chunky;

import org.popcraft.chunky.iterator.ChunkIterator;
import org.popcraft.chunky.iterator.ChunkIteratorFactory;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.shape.Shape;
import org.popcraft.chunky.shape.ShapeFactory;
import org.popcraft.chunky.util.ChunkCoordinate;
import org.popcraft.chunky.util.Pair;
import org.popcraft.chunky.util.RegionCache;
import org.popcraft.chunky.util.TranslationKey;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

public class GenerationTask implements Runnable {
    private static final int MAX_WORKING = 50;
    private final Chunky chunky;
    private final Selection selection;
    private final Shape shape;
    private final AtomicLong startTime = new AtomicLong();
    private final AtomicLong printTime = new AtomicLong();
    private final AtomicLong finishedChunks = new AtomicLong();
    private final AtomicLong totalChunks = new AtomicLong();
    private final Deque<Pair<Long, AtomicLong>> updateSamples = new ConcurrentLinkedDeque<>();
    private final Progress progress;
    private final RegionCache.WorldState worldState;
    private ChunkIterator chunkIterator;
    private boolean stopped, cancelled;
    private long prevTime;

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
        this.worldState = chunky.getRegionCache().getWorld(selection.world().getName());
    }

    private void update(int chunkX, int chunkZ, boolean loaded) {
        if (stopped) {
            return;
        }
        progress.chunkCount = finishedChunks.addAndGet(1);
        progress.percentComplete = 100f * progress.chunkCount / totalChunks.get();
        final long currentTime = System.currentTimeMillis();
        final Pair<Long, AtomicLong> bin = updateSamples.peekLast();
        if (loaded) {
            worldState.setGenerated(chunkX, chunkZ);
            if (bin != null && currentTime - bin.left() < 2e3) {
                bin.right().addAndGet(1);
            } else if (updateSamples.add(Pair.of(currentTime, new AtomicLong(1)))) {
                while (!updateSamples.isEmpty() && currentTime - updateSamples.peek().left() > 1e4) {
                    updateSamples.poll();
                }
            }
        }
        final Pair<Long, AtomicLong> oldest = updateSamples.peek();
        final long oldestTime = oldest == null ? currentTime : oldest.left();
        final long chunksLeft = totalChunks.get() - finishedChunks.get();
        final double timeDiff = (currentTime - oldestTime) / 1e3;
        if (chunksLeft > 0 && timeDiff < 1e-1) {
            return;
        }
        long sampleCount = 0;
        for (Pair<Long, AtomicLong> b : updateSamples) {
            sampleCount += b.right().get();
        }
        progress.rate = timeDiff > 0 ? sampleCount / timeDiff : 0;
        final long time;
        if (chunksLeft == 0) {
            time = (prevTime + (currentTime - startTime.get())) / 1000;
            progress.complete = true;
        } else {
            time = (long) (chunksLeft / progress.rate);
        }
        progress.hours = time / 3600;
        progress.minutes = (time - progress.hours * 3600) / 60;
        progress.seconds = time - progress.hours * 3600 - progress.minutes * 60;
        progress.chunkX = chunkX;
        progress.chunkZ = chunkZ;
        if (chunksLeft > 0 && (chunky.getOptions().isSilent() || ((currentTime - printTime.get()) / 1e3) < chunky.getOptions().getQuietInterval())) {
            return;
        }
        printTime.set(currentTime);
        progress.sendUpdate(chunky.getServer().getConsole());
    }

    @Override
    public void run() {
        final String poolThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName(String.format("Chunky-%s Thread", selection.world().getName()));
        final Semaphore working = new Semaphore(MAX_WORKING);
        startTime.set(System.currentTimeMillis());
        while (!stopped && chunkIterator.hasNext()) {
            final ChunkCoordinate chunk = chunkIterator.next();
            final int chunkCenterX = (chunk.x << 4) + 8;
            final int chunkCenterZ = (chunk.z << 4) + 8;
            if (!shape.isBounding(chunkCenterX, chunkCenterZ) || worldState.isGenerated(chunk.x, chunk.z)) {
                update(chunk.x, chunk.z, false);
                continue;
            }
            if (selection.world().isChunkGenerated(chunk.x, chunk.z)) {
                update(chunk.x, chunk.z, true);
                continue;
            }
            try {
                working.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                stop(cancelled);
                break;
            }
            selection.world().getChunkAtAsync(chunk.x, chunk.z).thenRun(() -> {
                working.release();
                update(chunk.x, chunk.z, true);
            });
        }
        if (stopped) {
            chunky.getServer().getConsole().sendMessagePrefixed(TranslationKey.TASK_STOPPED, selection.world().getName());
        } else {
            cancelled = true;
        }
        chunky.getConfig().saveTask(this);
        chunky.getGenerationTasks().remove(selection.world().getName());
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
