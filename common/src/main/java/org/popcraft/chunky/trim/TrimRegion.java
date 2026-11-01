package org.popcraft.chunky.trim;

import org.popcraft.chunky.nbt.LongTag;
import org.popcraft.chunky.nbt.TagType;
import org.popcraft.chunky.nbt.util.ChunkFilter;
import org.popcraft.chunky.nbt.util.RegionFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class TrimRegion {
    private static final int SECTOR_SIZE = 4096;
    private final RegionFile region;
    private final List<RegionFile> regions;

    public TrimRegion(final Path region, final Path poi, final Path entities, final boolean readsChunkData) {
        this.region = readsChunkData ? new RegionFile(region.toFile(), ChunkFilter.of(TagType.LONG, "InhabitedTime")) : new RegionFile(region.toFile(), true);
        this.regions = Stream.concat(Stream.of(this.region), Stream.of(poi, entities)
                        .filter(TrimRegion::isValid)
                        .map(path -> new RegionFile(path.toFile(), true)))
                .toList();
    }

    public boolean isFullyRead() {
        return region.isFullyRead();
    }

    public TrimChunk getTrimChunk(final int x, final int z) {
        final long inhabitedTime = region.getChunk(x, z)
                .map(chunk -> chunk.getData() instanceof final LongTag inhabited ? inhabited.value() : 0L)
                .orElse(0L);
        boolean exists = false;
        boolean unknown = false;
        long lastModified = 0;
        for (final RegionFile file : regions) {
            final Optional<RegionFile.Header> header = file.getHeader();
            if (header.isEmpty()) {
                exists = true;
                unknown = true;
                continue;
            }
            if (!header.get().hasChunk(x, z)) {
                continue;
            }
            exists = true;
            final long timestamp = header.get().getTimestamp(x, z);
            if (timestamp == 0) {
                unknown = true;
            }
            lastModified = Math.max(lastModified, timestamp);
        }
        return new TrimChunk(inhabitedTime, exists, unknown ? 0 : lastModified);
    }

    private static boolean isValid(final Path path) {
        try {
            return path != null && Files.exists(path) && Files.size(path) >= SECTOR_SIZE;
        } catch (final IOException e) {
            return false;
        }
    }
}
