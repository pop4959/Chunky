package org.popcraft.chunky;

import io.papermc.lib.PaperLib;
import org.bukkit.World;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.popcraft.chunky.Constants.*;

public class GenTask implements Runnable {
    private final Chunky chunky;
    private final World world;
    private final int radius;
    private final int centerX;
    private final int centerZ;
    private ChunkCoordinateIterator chunkCoordinates;
    private final static int FREQ = 50;
    private final AtomicLong startTime = new AtomicLong();
    private final AtomicLong printTime = new AtomicLong();
    private final AtomicLong finishedChunks = new AtomicLong();
    private final AtomicLong totalChunks = new AtomicLong();
    private final ConcurrentLinkedQueue<Long> chunkUpdateTimes10Sec = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean cancelled = new AtomicBoolean();

    public GenTask(Chunky chunky, World world, int radius, int centerX, int centerZ, long count) {
        this(chunky, world, radius, centerX, centerZ);
        this.chunkCoordinates = new ChunkCoordinateIterator(radius, centerX, centerZ, count);
        this.finishedChunks.set(count);
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
        double speed = chunkUpdateTimes10Sec.size() / timeDiff; // chunk updates in 1 second
        String message;
        if (chunksLeft == 0) {
            int total = (int) ((currentTime - startTime.get()) / 1e3);
            int totalHours = total / 3600;
            int totalMinutes = (total - totalHours * 3600) / 60;
            int totalSeconds = total - totalHours * 3600 - totalMinutes * 60;
            message = String.format(FORMAT_DONE, world, chunkNum, percentDone, totalHours, totalMinutes, totalSeconds);
        } else {
            int eta = (int) (chunksLeft / speed);
            int etaHours = eta / 3600;
            int etaMinutes = (eta - etaHours * 3600) / 60;
            int etaSeconds = eta - etaHours * 3600 - etaMinutes * 60;
            message = String.format(FORMAT_UPDATE, world, chunkNum, percentDone, etaHours, etaMinutes, etaSeconds, speed, chunkX, chunkZ);
        }
        chunky.getServer().getConsoleSender().sendMessage(message);
    }

    @Override
    public void run() {
        startTime.set(System.currentTimeMillis());
        final AtomicInteger working = new AtomicInteger();
        while (!cancelled.get() && chunkCoordinates.hasNext()) {
            if (working.get() < FREQ) {
                ChunkCoordinate chunkCoord = chunkCoordinates.next();
                if (PaperLib.isChunkGenerated(world, chunkCoord.x, chunkCoord.z)) {
                    printUpdate(world, chunkCoord.x, chunkCoord.z);
                    continue;
                }
                working.getAndIncrement();
                PaperLib.getChunkAtAsync(world, chunkCoord.x, chunkCoord.z).thenAccept(chunk -> {
                    working.getAndDecrement();
                    printUpdate(world, chunk.getX(), chunk.getZ());
                });
            }
        }
        while (working.get() > 0) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException ignored) {
            }
        }
        if (cancelled.get()) {
            chunky.getConfigStorage().saveTask(this);
            chunky.getServer().getConsoleSender().sendMessage(String.format(FORMAT_STOPPED, world.getName()));
        }
        chunky.getGenTasks().remove(this.getWorld());
    }

    void cancel() {
        cancelled.set(true);
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
}
