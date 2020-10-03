package org.popcraft.chunky;

import io.papermc.lib.PaperLib;
import org.bukkit.World;
import org.popcraft.chunky.iterator.ChunkIterator;
import org.popcraft.chunky.iterator.ChunkIteratorFactory;
import org.popcraft.chunky.shape.Shape;
import org.popcraft.chunky.shape.ShapeFactory;
import org.popcraft.chunky.util.TuinityLib;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

public class GenerationTask implements Runnable {
    private final Chunky chunky;
    private final World world;
    private final int radiusX, radiusZ, centerX, centerZ;
    private ChunkIterator chunkIterator;
    private Shape shape;
    private boolean stopped, cancelled, hasSaved;
    private long prevTime, totalTime;
    private final AtomicLong startTime = new AtomicLong();
    private final AtomicLong printTime = new AtomicLong();
    private final AtomicLong finishedChunks = new AtomicLong();
    private final AtomicLong totalChunks = new AtomicLong();
    private final ConcurrentLinkedQueue<Long> chunkUpdateTimes10Sec = new ConcurrentLinkedQueue<>();
    private static final int MAX_WORKING = 50;

    public GenerationTask(Chunky chunky, Selection selection, long count, long time) {
        this(chunky, selection);
        this.chunkIterator = ChunkIteratorFactory.getChunkIterator(selection, count);
        this.shape = ShapeFactory.getShape(selection);
        this.finishedChunks.set(count);
        this.prevTime = time;
    }

    public GenerationTask(Chunky chunky, Selection selection) {
        this.chunky = chunky;
        this.world = selection.world;
        this.radiusX = selection.radiusX;
        this.radiusZ = selection.radiusZ;
        this.centerX = selection.centerX;
        this.centerZ = selection.centerZ;
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
        chunkUpdateTimes10Sec.add(currentTime);
        while (currentTime - chunkUpdateTimes10Sec.peek() > 1e4) chunkUpdateTimes10Sec.poll();
        long chunksLeft = totalChunks.get() - finishedChunks.get();
        if (chunksLeft > 0 && (chunky.getSelection().silent || ((currentTime - printTime.get()) / 1e3) < chunky.getSelection().quiet)) {
            return;
        }
        printTime.set(currentTime);
        long oldestTime = chunkUpdateTimes10Sec.peek();
        double timeDiff = (currentTime - oldestTime) / 1e3;
        if (chunksLeft > 0 && timeDiff < 1e-1) {
            return;
        }
        double speed = chunkUpdateTimes10Sec.size() / timeDiff;
        String message;
        if (chunksLeft == 0) {
            long total = (prevTime + (currentTime - startTime.get())) / 1000;
            long totalHours = total / 3600;
            long totalMinutes = (total - totalHours * 3600) / 60;
            long totalSeconds = total - totalHours * 3600 - totalMinutes * 60;
            message = chunky.message("task_done", chunky.message("prefix"), world, chunkNum, percentDone, totalHours, totalMinutes, totalSeconds);
        } else {
            long eta = (long) (chunksLeft / speed);
            long etaHours = eta / 3600;
            long etaMinutes = (eta - etaHours * 3600) / 60;
            long etaSeconds = eta - etaHours * 3600 - etaMinutes * 60;
            message = chunky.message("task_update", chunky.message("prefix"), world, chunkNum, percentDone, etaHours, etaMinutes, etaSeconds, speed, chunkX, chunkZ);
        }
        chunky.getServer().getConsoleSender().sendMessage(message);
    }

    @Override
    public void run() {
        final String poolThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName(String.format("Chunky-%s Thread", world.getName()));
        final Semaphore working = new Semaphore(MAX_WORKING);
        startTime.set(System.currentTimeMillis());
        while (!stopped && chunkIterator.hasNext()) {
            ChunkCoordinate chunkCoord = chunkIterator.next();
            int xChunkCenter = (chunkCoord.x << 4) + 8;
            int zChunkCenter = (chunkCoord.z << 4) + 8;
            if (!shape.isBounding(xChunkCenter, zChunkCenter) || PaperLib.isPaper() && PaperLib.isChunkGenerated(world, chunkCoord.x, chunkCoord.z)) {
                printUpdate(world, chunkCoord.x, chunkCoord.z);
                continue;
            }
            try {
                working.acquire();
            } catch (InterruptedException e) {
                stop(cancelled, false);
                break;
            }
            PaperLib.getChunkAtAsync(world, chunkCoord.x, chunkCoord.z).thenAccept(chunk -> {
                working.release();
                printUpdate(world, chunk.getX(), chunk.getZ());
                if (TuinityLib.isTuinity() && TuinityLib.getDelayChunkUnloadsBy() > 0) {
                    chunky.getServer().getScheduler().scheduleSyncDelayedTask(chunky, chunk::unload);
                }
            });
        }
        totalTime += prevTime + (System.currentTimeMillis() - startTime.get());
        if (stopped) {
            chunky.getServer().getConsoleSender().sendMessage(chunky.message("task_stopped", chunky.message("prefix"), world.getName()));
        } else {
            this.cancelled = true;
        }
        stop(cancelled, true);
        chunky.getGenerationTasks().remove(this.getWorld());
        Thread.currentThread().setName(poolThreadName);
    }

    public void stop(boolean cancelled, boolean save) {
        this.stopped = true;
        this.cancelled = cancelled;
        if (save && !hasSaved) {
            chunky.getConfigStorage().saveTask(this);
            this.hasSaved = true;
        }
    }

    public World getWorld() {
        return world;
    }

    public int getRadiusX() {
        return radiusX;
    }

    public int getRadiusZ() {
        return radiusZ;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterZ() {
        return centerZ;
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
        return totalTime;
    }
}
