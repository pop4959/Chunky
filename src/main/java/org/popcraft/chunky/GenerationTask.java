package org.popcraft.chunky;

import io.papermc.lib.PaperLib;
import org.bukkit.World;
import org.popcraft.chunky.iterator.ChunkIterator;
import org.popcraft.chunky.iterator.ConcentricChunkIterator;
import org.popcraft.chunky.iterator.Loop2ChunkIterator;
import org.popcraft.chunky.iterator.SpiralChunkIterator;
import org.popcraft.chunky.shape.Circle;
import org.popcraft.chunky.shape.Diamond;
import org.popcraft.chunky.shape.Shape;
import org.popcraft.chunky.shape.Star;
import org.popcraft.chunky.shape.Triangle;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

public class GenerationTask implements Runnable {
    private final Chunky chunky;
    private final World world;
    private final int radius;
    private final int centerX;
    private final int centerZ;
    private ChunkIterator chunkIterator;
    private Shape shape;
    private boolean stopped, cancelled;
    private long prevTime, totalTime;
    private final AtomicLong startTime = new AtomicLong();
    private final AtomicLong printTime = new AtomicLong();
    private final AtomicLong finishedChunks = new AtomicLong();
    private final AtomicLong totalChunks = new AtomicLong();
    private final ConcurrentLinkedQueue<Long> chunkUpdateTimes10Sec = new ConcurrentLinkedQueue<>();
    private static final int MAX_WORKING = 50;

    public GenerationTask(Chunky chunky, World world, int radius, int centerX, int centerZ, long count, String iteratorType, long time) {
        this(chunky, world, radius, centerX, centerZ, iteratorType);
        switch (iteratorType) {
            case "loop":
                this.chunkIterator = new Loop2ChunkIterator(radius, centerX, centerZ, count);
                break;
            case "spiral":
                this.chunkIterator = new SpiralChunkIterator(radius, centerX, centerZ, count);
                break;
            case "concentric":
            default:
                this.chunkIterator = new ConcentricChunkIterator(radius, centerX, centerZ, count);
                break;
        }
        this.finishedChunks.set(count);
        this.prevTime = time;
    }

    public GenerationTask(Chunky chunky, World world, int radius, int centerX, int centerZ, String iteratorType) {
        this.chunky = chunky;
        this.world = world;
        this.radius = radius;
        this.centerX = centerX;
        this.centerZ = centerZ;
        switch (iteratorType) {
            case "loop":
                this.chunkIterator = new Loop2ChunkIterator(radius, centerX, centerZ);
                break;
            case "spiral":
                this.chunkIterator = new SpiralChunkIterator(radius, centerX, centerZ);
                break;
            case "concentric":
            default:
                this.chunkIterator = new ConcentricChunkIterator(radius, centerX, centerZ);
                break;
        }
        this.shape = new Diamond(chunkIterator);
        this.totalChunks.set(chunkIterator.total());
    }

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
        if (chunksLeft > 0 && (chunky.isSilent() || ((currentTime - printTime.get()) / 1e3) < chunky.getQuiet())) {
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
            message = chunky.message("task_done", world, chunkNum, percentDone, totalHours, totalMinutes, totalSeconds);
        } else {
            int eta = (int) (chunksLeft / speed);
            int etaHours = eta / 3600;
            int etaMinutes = (eta - etaHours * 3600) / 60;
            int etaSeconds = eta - etaHours * 3600 - etaMinutes * 60;
            message = chunky.message("task_update", world, chunkNum, percentDone, etaHours, etaMinutes, etaSeconds, speed, chunkX, chunkZ);
        }
        chunky.getServer().getConsoleSender().sendMessage(message);
    }

    @Override
    public void run() {
        final String poolThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName("Generation Task Thread");
        final Semaphore working = new Semaphore(MAX_WORKING);
        startTime.set(System.currentTimeMillis());
        while (!stopped && chunkIterator.hasNext()) {
            ChunkCoordinate chunkCoord = chunkIterator.next();
            if (!shape.isBounding(chunkCoord) || PaperLib.isPaper() && PaperLib.isChunkGenerated(world, chunkCoord.x, chunkCoord.z)) {
                printUpdate(world, chunkCoord.x, chunkCoord.z);
                continue;
            }
            try {
                working.acquire();
            } catch (InterruptedException e) {
                stop(cancelled);
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
            chunky.getServer().getConsoleSender().sendMessage(chunky.message("task_stopped", world.getName()));
        } else {
            this.cancelled = true;
        }
        chunky.getConfigStorage().saveTask(this);
        chunky.getGenerationTasks().remove(this.getWorld());
        Thread.currentThread().setName(poolThreadName);
    }

    void stop(boolean cancelled) {
        this.stopped = true;
        this.cancelled = cancelled;
    }

    public World getWorld() {
        return world;
    }

    public int getRadius() {
        return radius;
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

    public boolean isCancelled() {
        return cancelled;
    }

    public long getTotalTime() {
        return totalTime;
    }
}
