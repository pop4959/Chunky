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

    public RegionChunkIterator(final Selection selection, @SuppressWarnings({"unused", "java:S1172"}) final long count) {
        this(selection);
        // TODO: Implement
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
//        System.out.printf("Region center %d %d, Radius %d%n", centerRegionX, centerRegionZ, radiusRegions);
//        System.out.printf("Chunk center %d %d, Radius %d%n", centerChunkX, centerChunkZ, radiusChunks);
//        System.out.printf("Iterator edges (%d %d) -> (%d, %d)%n", minChunkX, minChunkZ, maxChunkX, maxChunkZ);
//        System.out.printf("Iterator total %d%n", totalChunks);
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
        if (!hasNextRegion) {
            return null;
        }
        final RegionChunkProgress regionChunkProgress = new RegionChunkProgress(regionX, regionZ);
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

        public RegionChunkProgress(final int x, final int z, final int count) {
            this(x, z);
            if (count <= 0) {
                return;
            }
            this.current = count;
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
//            System.out.printf("%s%n", name);
//            System.out.printf("Region edges (%d %d) -> (%d, %d)%n", lowEdgeX, lowEdgeZ, highEdgeX, highEdgeZ);
//            System.out.printf("Edges (%d %d) -> (%d, %d)%n", minX, minZ, maxX, maxZ);
//            System.out.printf("Deltas %d %d%n", sizeX, sizeZ);
//            System.out.printf("Total %d%n", total);
//            System.out.printf("Full %b%n", full);
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
