package org.popcraft.chunky;

import io.papermc.lib.PaperLib;
import org.bukkit.World;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class GenTask implements Runnable {
    private final Chunky chunky;
    private final World world;
    private final int radius;
    private final static int FREQ = 50;
    private final static String FORMAT_UPDATE = "[Chunky] Task running for %s. Processed: %d chunks (%.2f%%), ETA: %01d:%02d:%02d, Rate: %.1f cps, Current: %d, %d";
    private final static String FORMAT_DONE = "[Chunky] Task finished for %s. Processed: %d chunks (%.2f%%), Total time: %01d:%02d:%02d";
    private final AtomicLong startTime = new AtomicLong();
    private final AtomicLong finishedChunks = new AtomicLong();
    private final AtomicLong totalChunks = new AtomicLong();
    private final ConcurrentLinkedQueue<Long> chunkUpdateTimes10Sec = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean cancelled = new AtomicBoolean();

    public GenTask(Chunky chunky, World world, int radius) {
        this.chunky = chunky;
        this.world = world;
        this.radius = radius;
    }

    private void printUpdate(World chunkWorld, int chunkX, int chunkZ) {
        String world = chunkWorld.getName();
        long chunkNum = finishedChunks.addAndGet(1);
        double percentDone = 100f * chunkNum / totalChunks.get();
        long currentTime = System.currentTimeMillis();
        chunkUpdateTimes10Sec.add(currentTime);
        while (currentTime - chunkUpdateTimes10Sec.peek() > 1e4 /* 10 seconds */) chunkUpdateTimes10Sec.poll();
        long oldestTime = chunkUpdateTimes10Sec.peek();
        double timeDiff = (currentTime - oldestTime) / 1e3;
        double speed = chunkUpdateTimes10Sec.size() / timeDiff; // chunk updates in 1 second
        long chunksLeft = totalChunks.get() - finishedChunks.get();
        String message;
        if (totalChunks.get() == finishedChunks.get()) {
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
        chunky.getServer().getConsoleSender().sendMessage("Preparing...");
        final ChunkCoordinateIterator chunkCoordinates = new ChunkCoordinateIterator(radius);
        totalChunks.set(chunkCoordinates.count());
        finishedChunks.set(0);
        chunkUpdateTimes10Sec.clear();
        chunky.getServer().getConsoleSender().sendMessage("Starting...");
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
    }

    void cancel() {
        cancelled.set(true);
    }
}
