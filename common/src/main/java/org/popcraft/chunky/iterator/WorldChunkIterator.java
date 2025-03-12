package org.popcraft.chunky.iterator;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.nbt.StringTag;
import org.popcraft.chunky.nbt.TagType;
import org.popcraft.chunky.nbt.util.ChunkFilter;
import org.popcraft.chunky.nbt.util.RegionFile;
import org.popcraft.chunky.util.ChunkCoordinate;
import org.popcraft.chunky.util.Hilbert;
import org.popcraft.chunky.util.Parameter;
import org.popcraft.chunky.util.TranslationKey;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Stream;

public class WorldChunkIterator implements ChunkIterator {
    private final Chunky chunky;
    private final int minRegionX;
    private final int minRegionZ;
    private final int maxRegionX;
    private final int maxRegionZ;
    private final int minChunkX;
    private final int minChunkZ;
    private final int maxChunkX;
    private final int maxChunkZ;
    private final String worldName;
    private final Queue<ChunkCoordinate> chunks;
    private final AtomicLong total = new AtomicLong();
    private final Path savePath;
    private final Path regionPath;
    private final String name;

    public WorldChunkIterator(final Selection selection) {
        this.chunky = selection.chunky();
        final int centerRegionX = selection.centerRegionX();
        final int centerRegionZ = selection.centerRegionZ();
        final int radiusRegionsX = selection.radiusRegionsX();
        final int radiusRegionsZ = selection.radiusRegionsZ();
        this.minRegionX = centerRegionX - radiusRegionsX;
        this.minRegionZ = centerRegionZ - radiusRegionsZ;
        this.maxRegionX = centerRegionX + radiusRegionsX;
        this.maxRegionZ = centerRegionZ + radiusRegionsZ;
        final int centerChunkX = selection.centerChunkX();
        final int centerChunkZ = selection.centerChunkZ();
        final int radiusChunksX = selection.radiusChunksX();
        final int radiusChunksZ = selection.radiusChunksZ();
        this.minChunkX = centerChunkX - radiusChunksX;
        this.minChunkZ = centerChunkZ - radiusChunksZ;
        this.maxChunkX = centerChunkX + radiusChunksX;
        this.maxChunkZ = centerChunkZ + radiusChunksZ;
        this.chunks = new LinkedList<>();
        this.worldName = selection.world().getName();
        final String saveFile = worldName.substring(worldName.indexOf(':') + 1);
        this.savePath = selection.chunky().getConfig().getDirectory().resolve(String.format("%s.csv", saveFile));
        this.regionPath = selection.world().getRegionDirectory().orElse(null);
        this.name = Parameter.of(PatternType.CSV, saveFile).toString();
    }

    @Override
    public boolean hasNext() {
        return !chunks.isEmpty();
    }

    @Override
    public ChunkCoordinate next() {
        if (chunks.isEmpty()) {
            throw new NoSuchElementException();
        }
        return chunks.poll();
    }

    @Override
    public long total() {
        return total.get();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean process() {
        if (regionPath == null) {
            return false;
        }
        final long startTime = System.currentTimeMillis();
        final AtomicLong updateTime = new AtomicLong(startTime);
        final Function<String, Boolean> chunkStatusFilter = chunky.getConfig().isForceLoadExistingChunks() ?
                status -> !("minecraft:empty".equals(status) || "empty".equals(status)) :
                status -> "minecraft:full".equals(status) || "full".equals(status);
        final StringBuilder saveData = new StringBuilder();
        try (final Stream<Path> files = Files.list(regionPath)) {
            final List<Path> regions = files
                    .filter(file -> {
                        final ChunkCoordinate regionCoordinate = ChunkCoordinate.fromRegionFile(file.getFileName().toString())
                                .orElse(null);
                        return regionCoordinate != null && regionCoordinate.x() >= minRegionX && regionCoordinate.x() <= maxRegionX && regionCoordinate.z() >= minRegionZ && regionCoordinate.z() <= maxRegionZ;
                    })
                    .toList();
            final long totalRegions = regions.size();
            final AtomicLong finishedRegions = new AtomicLong();
            for (final Path region : regions) {
                final ChunkCoordinate regionCoordinate = ChunkCoordinate.fromRegionFile(region.getFileName().toString())
                        .orElseThrow(IllegalStateException::new);
                final int regionX = regionCoordinate.x();
                final int regionZ = regionCoordinate.z();
                final RegionFile regionFile = new RegionFile(region.toFile(), ChunkFilter.of(TagType.STRING, "Status"));
                for (final ChunkCoordinate offset : Hilbert.chunkCoordinateOffsets()) {
                    final ChunkCoordinate chunkCoordinate = new ChunkCoordinate((regionX << 5) + offset.x(), (regionZ << 5) + offset.z());
                    if (chunkCoordinate.x() < minChunkX || chunkCoordinate.x() > maxChunkX || chunkCoordinate.z() < minChunkZ || chunkCoordinate.z() > maxChunkZ) {
                        continue;
                    }
                    regionFile.getChunk(chunkCoordinate.x(), chunkCoordinate.z()).ifPresent(chunk -> {
                        final boolean generated = Optional.ofNullable(chunk.getData())
                                .filter(StringTag.class::isInstance)
                                .map(StringTag.class::cast)
                                .map(StringTag::value)
                                .map(chunkStatusFilter)
                                .orElse(false);
                        if (generated) {
                            chunks.add(chunkCoordinate);
                            saveData.append(chunkCoordinate.x()).append(',').append(chunkCoordinate.z()).append('\n');
                            total.incrementAndGet();
                        }
                    });
                }
                finishedRegions.getAndIncrement();
                if (!chunky.getConfig().isSilent()) {
                    final long currentTime = System.currentTimeMillis();
                    final boolean updateIntervalElapsed = ((currentTime - updateTime.get()) / 1e3) > chunky.getConfig().getUpdateInterval();
                    if (updateIntervalElapsed || finishedRegions.get() == totalRegions) {
                        chunky.getServer().getConsole().sendMessagePrefixed(TranslationKey.TASK_TRIM_UPDATE, worldName, finishedRegions.get(), String.format("%.2f", 100f * finishedRegions.get() / totalRegions));
                        updateTime.set(currentTime);
                    }
                }
                if (Thread.interrupted()) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            Files.writeString(savePath, saveData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
