package org.popcraft.chunky.iterator;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.util.ChunkCoordinate;
import org.popcraft.chunky.util.Hilbert;

import java.util.NoSuchElementException;

public class RegionChunkIterator implements ChunkIterator {
    private final int centerRegionX;
    private final int centerRegionZ;
    private final int radiusRegions;
    private final int minChunkX;
    private final int minChunkZ;
    private final int maxChunkX;
    private final int maxChunkZ;
    private final long totalChunks;
    private RegionChunkProgress currentRegionProgress;
    private int regionX;
    private int regionZ;
    private int annulusRegions;
    private int spanRegions;
    private int downRegions;
    private int leftRegions;
    private int upRegions;
    private int rightRegions;
    private boolean hasNextRegion = true;
    private boolean hasNext = true;

    public RegionChunkIterator(final Selection selection, final long count) {
        this(selection);
        if (count <= 0) {
            return;
        }
        this.currentRegionProgress = null;
        this.regionX = centerRegionX;
        this.regionZ = centerRegionZ;
        this.annulusRegions = 0;
        this.spanRegions = 0;
        this.downRegions = this.leftRegions = this.upRegions = this.rightRegions = 0;
        this.hasNextRegion = true;
        this.hasNext = true;
        long countRemainingChunks = count;
        final long estimatedRegionCount = count / 1024;
        int estimatedDiameterRegions = (int) Math.floor(Math.sqrt(estimatedRegionCount));
        if (estimatedDiameterRegions % 2 == 0) {
            --estimatedDiameterRegions;
        }
        if (estimatedDiameterRegions > 2) {
            final int skipDiameterRegions = estimatedDiameterRegions - 2;
            this.annulusRegions = skipDiameterRegions / 2 + 1;
            this.regionX += annulusRegions;
            this.regionZ += annulusRegions - 1;
            this.spanRegions = 2 * annulusRegions;
            ++this.downRegions;
            final long skipChunks = ((long) skipDiameterRegions * skipDiameterRegions) * 1024;
            countRemainingChunks -= skipChunks;
        }
        currentRegionProgress = nextRegionChunkProgress(countRemainingChunks);
        countRemainingChunks -= Math.min(countRemainingChunks, currentRegionProgress == null ? 0 : currentRegionProgress.total());
        while (currentRegionProgress != null && !currentRegionProgress.hasNext()) {
            currentRegionProgress = nextRegionChunkProgress(countRemainingChunks);
            countRemainingChunks -= Math.min(countRemainingChunks, currentRegionProgress == null ? 0 : currentRegionProgress.total());
        }
        if (currentRegionProgress == null) {
            hasNext = false;
        }
    }

    public RegionChunkIterator(final Selection selection) {
        this.centerRegionX = selection.centerRegionX();
        this.centerRegionZ = selection.centerRegionZ();
        this.radiusRegions = selection.radiusRegionsX();
        final int centerChunkX = selection.centerChunkX();
        final int centerChunkZ = selection.centerChunkZ();
        final int radiusChunks = selection.radiusChunksX();
        this.minChunkX = centerChunkX - radiusChunks;
        this.minChunkZ = centerChunkZ - radiusChunks;
        this.maxChunkX = centerChunkX + radiusChunks;
        this.maxChunkZ = centerChunkZ + radiusChunks;
        this.regionX = centerRegionX;
        this.regionZ = centerRegionZ;
        final long diameterChunks = selection.diameterChunksX();
        this.totalChunks = diameterChunks * diameterChunks;
        this.currentRegionProgress = nextRegionChunkProgress();
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public ChunkCoordinate next() {
        if (!hasNext) {
            throw new NoSuchElementException();
        }
        final ChunkCoordinate chunkCoord = currentRegionProgress.next();
        while (currentRegionProgress != null && !currentRegionProgress.hasNext()) {
            currentRegionProgress = nextRegionChunkProgress();
        }
        if (currentRegionProgress == null) {
            hasNext = false;
        }
        return chunkCoord;
    }

    private RegionChunkProgress nextRegionChunkProgress() {
        return nextRegionChunkProgress(0);
    }

    private RegionChunkProgress nextRegionChunkProgress(final long count) {
        if (!hasNextRegion) {
            return null;
        }
        final RegionChunkProgress regionChunkProgress = new RegionChunkProgress(regionX, regionZ, count);
        if (regionX == centerRegionX + annulusRegions && regionZ == centerRegionZ + annulusRegions) {
            ++annulusRegions;
            ++regionX;
            ++regionZ;
            if (annulusRegions > radiusRegions) {
                hasNextRegion = false;
            }
            spanRegions = 2 * annulusRegions;
            downRegions = leftRegions = upRegions = rightRegions = 0;
        }
        if (downRegions < spanRegions) {
            --regionZ;
            ++downRegions;
        } else if (leftRegions < spanRegions) {
            --regionX;
            ++leftRegions;
        } else if (upRegions < spanRegions) {
            ++regionZ;
            ++upRegions;
        } else if (rightRegions < spanRegions) {
            ++regionX;
            ++rightRegions;
        }
        return regionChunkProgress;
    }

    @Override
    public long total() {
        return totalChunks;
    }

    @Override
    public String name() {
        return PatternType.REGION;
    }

    public final class RegionChunkProgress implements ChunkIterator {
        private final int minX;
        private final int minZ;
        private final int sizeZ;
        private final int total;
        private final boolean full;
        private final String name;
        private int current;
        private int offsetX;
        private int offsetZ;
        private boolean hasNext = true;

        public RegionChunkProgress(final int x, final int z, final long count) {
            this(x, z);
            if (count <= 0) {
                return;
            }
            this.current = (int) Math.min(count, 1024);
            if (!full && sizeZ > 0) {
                offsetX = current / sizeZ;
                offsetZ = current % sizeZ;
            }
            if (current >= total) {
                this.hasNext = false;
            }
        }

        public RegionChunkProgress(final int x, final int z) {
            final int lowEdgeX = x << 5;
            final int lowEdgeZ = z << 5;
            final int highEdgeX = lowEdgeX + 31;
            final int highEdgeZ = lowEdgeZ + 31;
            this.minX = Math.max(lowEdgeX, minChunkX);
            this.minZ = Math.max(lowEdgeZ, minChunkZ);
            final int maxX = Math.min(highEdgeX, maxChunkX);
            final int maxZ = Math.min(highEdgeZ, maxChunkZ);
            final int sizeX = maxX - minX + 1;
            if (minX > highEdgeX || minZ > highEdgeZ || maxX < lowEdgeX || maxZ < lowEdgeZ) {
                this.sizeZ = 0;
                this.total = 0;
                this.full = false;
                this.hasNext = false;
            } else {
                this.sizeZ = maxZ - minZ + 1;
                this.total = sizeX * sizeZ;
                this.full = total == 1024;
            }
            this.name = "region_chunk_progress_%d_%d".formatted(x, z);
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public ChunkCoordinate next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            final ChunkCoordinate chunkCoord;
            if (full) {
                final ChunkCoordinate offset = Hilbert.regionDistanceToChunkCoordinateOffset(current);
                chunkCoord = new ChunkCoordinate(minX + offset.x(), minZ + offset.z());
            } else {
                chunkCoord = new ChunkCoordinate(minX + offsetX, minZ + offsetZ);
                if (++offsetZ >= sizeZ) {
                    offsetZ = 0;
                    ++offsetX;
                }
            }
            ++current;
            if (current >= total) {
                hasNext = false;
            }
            return chunkCoord;
        }

        @Override
        public long total() {
            return total;
        }

        @Override
        public String name() {
            return name;
        }
    }
}
