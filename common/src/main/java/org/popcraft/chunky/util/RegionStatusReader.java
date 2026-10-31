package org.popcraft.chunky.util;

import org.popcraft.chunky.nbt.CompoundTag;
import org.popcraft.chunky.nbt.StringTag;
import org.popcraft.chunky.nbt.util.Chunk;
import org.popcraft.chunky.nbt.util.ChunkFilter;
import org.popcraft.chunky.nbt.util.RegionFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public final class RegionStatusReader implements AutoCloseable {
    private static final boolean UPDATE_CHUNK_NBT = Boolean.getBoolean("chunky.updateChunkNbt");
    private static final boolean LOAD_OUTDATED_CHUNKS = Boolean.getBoolean("chunky.loadOutdatedChunks");
    private static final int THREADS = Math.max(1, Input.tryInteger(System.getProperty("chunky.regionReaderThreads")).orElse(Runtime.getRuntime().availableProcessors() / 4));
    private final Path regionDirectory;
    private final RegionCache.WorldState worldState;
    private final ExecutorService executor;
    private final Map<Long, CompletableFuture<Void>> regions = new ConcurrentHashMap<>();

    public RegionStatusReader(final String worldName, final Path regionDirectory, final RegionCache.WorldState worldState) {
        this.regionDirectory = regionDirectory;
        this.worldState = worldState;
        final AtomicInteger count = new AtomicInteger();
        this.executor = Executors.newFixedThreadPool(THREADS, runnable -> {
            final Thread thread = new Thread(runnable, "Chunky Region Reader - %s #%d".formatted(worldName, count.incrementAndGet()));
            thread.setDaemon(true);
            return thread;
        });
    }

    public CompletableFuture<Boolean> isGenerated(final int x, final int z) {
        if (UPDATE_CHUNK_NBT || LOAD_OUTDATED_CHUNKS) {
            return CompletableFuture.completedFuture(false);
        }
        final int regionX = x >> 5;
        final int regionZ = z >> 5;
        return regions.computeIfAbsent(ChunkMath.pack(regionX, regionZ), ignored -> CompletableFuture.runAsync(() -> read(regionX, regionZ), executor))
                .thenApply(ignored -> worldState.isGenerated(x, z))
                .exceptionally(ignored -> false);
    }

    private void read(final int regionX, final int regionZ) {
        final Path file = regionDirectory.resolve("r.%d.%d.mca".formatted(regionX, regionZ));
        if (!Files.exists(file)) {
            return;
        }
        final RegionFile regionFile = new RegionFile(file.toFile(), ChunkFilter.ofTags("xPos", "zPos", "Status"));
        for (final Chunk chunk : regionFile.getChunks()) {
            if (chunk.getData() instanceof final CompoundTag data && isFull(data, chunk.getX(), chunk.getZ())) {
                worldState.setGenerated(chunk.getX(), chunk.getZ());
            }
        }
    }

    private static boolean isFull(final CompoundTag data, final int x, final int z) {
        final boolean valid = data.getInt("xPos").filter(xPos -> xPos.value() == x).isPresent()
                && data.getInt("zPos").filter(zPos -> zPos.value() == z).isPresent();
        final boolean full = data.getString("Status").map(StringTag::value)
                .filter(status -> "minecraft:full".equals(status) || "full".equals(status))
                .isPresent();
        return valid && full;
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
