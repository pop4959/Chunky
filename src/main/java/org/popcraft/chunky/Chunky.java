package org.popcraft.chunky;

import io.papermc.lib.PaperLib;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public final class Chunky extends JavaPlugin {

    private final static int CHUNK_SIZE = 16;

    @Override
    public void onEnable() {
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private static class ChunkLocation {
        public final int x, z;

        public ChunkLocation(int x, int z) {
            this.x = x;
            this.z = z;
        }
    }

    final String printUpdateFormat = "%d";
    final String printDoneFormat = "Done!";
    final AtomicInteger finishedChunks = new AtomicInteger();
    final int FREQ = 50;

    private void printUpdate() {
        this.getServer().getConsoleSender().sendMessage(String.format(printUpdateFormat, finishedChunks.addAndGet(1)));
    }

    private void printDone() {
        this.getServer().getConsoleSender().sendMessage(printDoneFormat);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final World world = sender instanceof Player ? ((Player) sender).getWorld() : this.getServer().getWorlds().get(0);
        final int radius = Integer.parseInt(args[0]);
        final CompletableFuture<Void> genTask = new CompletableFuture<>();
        genTask.thenRun(this::printDone);
        this.getServer().getScheduler().runTaskAsynchronously(this, () -> {
            Queue<ChunkLocation> chunkLocations = new LinkedList<>();
            this.getServer().getConsoleSender().sendMessage("Preparing...");
            for (int x = -radius; x < radius; x += CHUNK_SIZE) {
                for (int z = -radius; z < radius; z += CHUNK_SIZE) {
                    chunkLocations.add(new ChunkLocation(x << 4, z << 4));
                }
            }
            this.getServer().getConsoleSender().sendMessage("Starting...");
            final AtomicInteger working = new AtomicInteger();
            while (!chunkLocations.isEmpty()) {
                if (working.get() < FREQ) {
                    working.getAndIncrement();
                    ChunkLocation chunkLocation = chunkLocations.poll();
                    CompletableFuture<Chunk> chunkFuture = PaperLib.getChunkAtAsync(world, chunkLocation.x, chunkLocation.z);
                    chunkFuture.thenRun(this::printUpdate).thenRun(working::getAndDecrement);
                }
            }
            genTask.complete(null);
        });
        return true;
    }
}
