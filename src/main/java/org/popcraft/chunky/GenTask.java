package org.popcraft.chunky;

import io.papermc.lib.PaperLib;
import org.bukkit.World;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

public class GenTask implements Runnable {
    private final Chunky chunky;
    private final World world;
    private final int radius;
    private final int centerX;
    private final int centerZ;
    private ChunkCoordinateIterator chunkCoordinates;
    private boolean stopped, cancelled;
    private long prevTime, totalTime;
    private final AtomicLong startTime = new AtomicLong();
    private final AtomicLong printTime = new AtomicLong();
    private final AtomicLong finishedChunks = new AtomicLong();
    private final AtomicLong totalChunks = new AtomicLong();
    private final ConcurrentLinkedQueue<Long> chunkUpdateTimes10Sec = new ConcurrentLinkedQueue<>();
    private static final int MAX_WORKING = 50;

    public GenTask(Chunky chunky, World world, int radius, int centerX, int centerZ, long count, long time) {
        this(chunky, world, radius, centerX, centerZ);
        this.chunkCoordinates = new ChunkCoordinateIterator(radius, centerX, centerZ, count);
        this.finishedChunks.set(count);
        this.prevTime = time;
    }

    public GenTask(Chunky chunky, World world, int radius, int centerX, int centerZ) {
        this.chunky = chunky;
        this.world = world;
        this.radius = radius;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.chunkCoordinates = new ChunkCoordinateIterator(radius, centerX, centerZ);
        this.totalChunks.set(chunkCoordinates.count());
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
        while (currentTime - chunkUpdateTimes10Sec.peek() > 1e4 /* 10 seconds */) chunkUpdateTimes10Sec.poll();
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
        double speed = chunkUpdateTimes10Sec.size() / timeDiff; // chunk updates in 1 second
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
        final Semaphore working = new Semaphore(MAX_WORKING);
        startTime.set(System.currentTimeMillis());
        while (!stopped && chunkCoordinates.hasNext()) {
            ChunkCoordinate chunkCoord = chunkCoordinates.next();
            if (PaperLib.isPaper() && PaperLib.isChunkGenerated(world, chunkCoord.x, chunkCoord.z)) {
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
        chunky.getGenTasks().remove(this.getWorld());
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

    public ChunkCoordinateIterator getChunkCoordinateIterator() {
        return chunkCoordinates;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public long getTotalTime() {
        return totalTime;
    }
}
