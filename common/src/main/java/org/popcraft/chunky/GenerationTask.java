package org.popcraft.chunky;

import org.popcraft.chunky.iterator.ChunkIterator;
import org.popcraft.chunky.iterator.ChunkIteratorFactory;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.shape.Shape;
import org.popcraft.chunky.shape.ShapeFactory;
import org.popcraft.chunky.util.ChunkCoordinate;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import static org.popcraft.chunky.Chunky.translate;

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
    }

    @SuppressWarnings("ConstantConditions")
    private void printUpdate(World chunkWorld, int chunkX, int chunkZ) {
        if (stopped) {
            return;
        }
        String world = chunkWorld.getName();
        long chunkNum = finishedChunks.addAndGet(1);
        double percentDone = 100f * chunkNum / totalChunks.get();
        long currentTime = System.currentTimeMillis();
        chunkUpdateTimes.add(currentTime);
        while (currentTime - chunkUpdateTimes.peek() > 1e4) {
            chunkUpdateTimes.poll();
        }
        long chunksLeft = totalChunks.get() - finishedChunks.get();
        if (chunksLeft > 0 && (chunky.getOptions().isSilent() || ((currentTime - printTime.get()) / 1e3) < chunky.getOptions().getQuietInterval())) {
            return;
        }
        printTime.set(currentTime);
        long oldestTime = chunkUpdateTimes.peek();
        double timeDiff = (currentTime - oldestTime) / 1e3;
        if (chunksLeft > 0 && timeDiff < 1e-1) {
            return;
        }
        double speed = chunkUpdateTimes.size() / timeDiff;
        Sender console = chunky.getPlatform().getServer().getConsoleSender();
        if (chunksLeft == 0) {
            long total = (prevTime + (currentTime - startTime.get())) / 1000;
            long totalHours = total / 3600;
            long totalMinutes = (total - totalHours * 3600) / 60;
            long totalSeconds = total - totalHours * 3600 - totalMinutes * 60;
            console.sendMessage("task_done", translate("prefix"), world, chunkNum, percentDone, totalHours, totalMinutes, totalSeconds);
        } else {
            long eta = (long) (chunksLeft / speed);
            long etaHours = eta / 3600;
            long etaMinutes = (eta - etaHours * 3600) / 60;
            long etaSeconds = eta - etaHours * 3600 - etaMinutes * 60;
            console.sendMessage("task_update", translate("prefix"), world, chunkNum, percentDone, etaHours, etaMinutes, etaSeconds, speed, chunkX, chunkZ);
        }
    }

    @Override
    public void run() {
        final String poolThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName(String.format("Chunky-%s Thread", selection.world().getName()));
        final Semaphore working = new Semaphore(MAX_WORKING);
        startTime.set(System.currentTimeMillis());
        while (!stopped && chunkIterator.hasNext()) {
            final ChunkCoordinate chunkCoord = chunkIterator.next();
            int xChunkCenter = (chunkCoord.x << 4) + 8;
            int zChunkCenter = (chunkCoord.z << 4) + 8;
            if (!shape.isBounding(xChunkCenter, zChunkCenter) || selection.world().isChunkGenerated(chunkCoord.x, chunkCoord.z)) {
                printUpdate(selection.world(), chunkCoord.x, chunkCoord.z);
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
                printUpdate(selection.world(), chunkCoord.x, chunkCoord.z);
            });
        }
        if (stopped) {
            chunky.getPlatform().getServer().getConsoleSender().sendMessage("task_stopped", translate("prefix"), selection.world().getName());
        } else {
            this.cancelled = true;
        }
        chunky.getConfig().saveTask(this);
        chunky.getGenerationTasks().remove(selection.world());
        Thread.currentThread().setName(poolThreadName);
    }

    public void stop(boolean cancelled) {
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
}
