package org.popcraft.chunky.iterator;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.nbt.IntTag;
import org.popcraft.chunky.nbt.StringTag;
import org.popcraft.chunky.nbt.util.Chunk;
import org.popcraft.chunky.nbt.util.RegionFile;
import org.popcraft.chunky.util.ChunkCoordinate;
import org.popcraft.chunky.util.Hilbert;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.Parameter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class WorldChunkIterator implements ChunkIterator {
    private final Selection selection;
    private final Queue<ChunkCoordinate> chunks;
    private final AtomicLong total = new AtomicLong();
    private final String fileName;
    private final String name;

    public WorldChunkIterator(final Selection selection) {
        this.chunks = new LinkedList<>();
        final String worldName = selection.world().getName();
        this.fileName = worldName.substring(worldName.indexOf(':') + 1);
        this.name = Parameter.of(PatternType.CSV, fileName).toString();
        this.selection = selection;
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
    public void postInitialization() {
        final Path filePath = selection.chunky().getConfig().getDirectory().resolve(String.format("%s.csv", fileName));
        final StringBuilder csvData = new StringBuilder();
        final Optional<Path> regionPath = selection.world().getRegionDirectory();
        try {
            if (regionPath.isPresent()) {
                try (final Stream<Path> files = Files.list(regionPath.get())) {
                    final List<Path> regions = files
                            .filter(file -> tryRegionCoordinate(file.getFileName().toString()).isPresent())
                            .toList();
                    final long totalRegions = regions.size();
                    System.out.println(totalRegions);
                    for (final Path region : regions) {
                        System.out.println(region.getFileName().toString());
                        final RegionFile regionFile = new RegionFile(region.toFile());
                        final Set<ChunkCoordinate> regionChunks = new HashSet<>();
                        for (final Chunk chunk : regionFile.getChunks()) {
                            final boolean generated = chunk.getData().getString("Status")
                                    .map(StringTag::value)
                                    .map(status -> "minecraft:full".equals(status) || "full".equals(status))
                                    .orElse(false);
                            if (generated) {
                                final Optional<IntTag> xPos = chunk.getData().getInt("xPos");
                                final Optional<IntTag> zPos = chunk.getData().getInt("zPos");
                                if (xPos.isPresent() && zPos.isPresent()) {
                                    final int x = xPos.get().value();
                                    final int z = zPos.get().value();
                                    regionChunks.add(new ChunkCoordinate(x, z));
                                }
                            }
                        }
                        if (!regionChunks.isEmpty()) {
                            final ChunkCoordinate regionCoordinates = tryRegionCoordinate(region.getFileName().toString()).orElse(null);
                            if (regionCoordinates == null) {
                                throw new IllegalStateException("Region coordinates could not be determined for region file");
                            }
                            final int regionX = regionCoordinates.x();
                            final int regionZ = regionCoordinates.z();
                            for (final ChunkCoordinate offset : Hilbert.chunkCoordinateOffsets()) {
                                final ChunkCoordinate chunkCoordinate = new ChunkCoordinate((regionX << 5) + offset.x(), (regionZ << 5) + offset.z());
                                if (regionChunks.contains(chunkCoordinate)) {
                                    chunks.add(chunkCoordinate);
                                    csvData.append(chunkCoordinate.x()).append(',').append(chunkCoordinate.z()).append('\n');
                                    total.incrementAndGet();
                                }
                            }
                        }
                    }
                }
            }
            Files.writeString(filePath, csvData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(total.get());
    }

    private Optional<ChunkCoordinate> tryRegionCoordinate(final String regionFileName) {
        if (!regionFileName.startsWith("r.")) {
            return Optional.empty();
        }
        final int extension = regionFileName.indexOf(".mca");
        if (extension < 2) {
            return Optional.empty();
        }
        final String regionCoordinates = regionFileName.substring(2, extension);
        final int separator = regionCoordinates.indexOf('.');
        final Optional<Integer> regionX = Input.tryInteger(regionCoordinates.substring(0, separator));
        final Optional<Integer> regionZ = Input.tryInteger(regionCoordinates.substring(separator + 1));
        if (regionX.isPresent() && regionZ.isPresent()) {
            return Optional.of(new ChunkCoordinate(regionX.get(), regionZ.get()));
        }
        return Optional.empty();
    }
}
