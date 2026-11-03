package org.popcraft.chunky.util;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.nbt.CompoundTag;
import org.popcraft.chunky.nbt.util.Chunk;
import org.popcraft.chunky.nbt.util.ChunkFilter;
import org.popcraft.chunky.nbt.util.RegionFile;
import org.popcraft.chunky.shape.Shape;
import org.popcraft.chunky.shape.ShapeFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public final class GenerationMap {
    public static final int FULL = 0;
    public static final int UNFINISHED = 1;
    public static final int MISSING = 2;
    public static final int INCOMPLETE = 3;
    public static final int INVALID = 4;
    private static final int STATUSES = 5;
    private static final int[] COLORS = {0x7FB238, 0x4F8FE0, 0x000000, 0xE5E533, 0xE03C3C, 0xFFFFFF};
    private static final int OUTSIDE = STATUSES;
    private static final int MAX_SIZE = 4096;
    private static final ChunkFilter FILTER = ChunkFilter.ofTags("xPos", "zPos", "Status", "isLightOn", "LastUpdate");
    private static final int THREADS = Math.max(1, Runtime.getRuntime().availableProcessors() / 4);
    private final Selection selection;
    private final Shape shape;
    private final int minChunkX;
    private final int minChunkZ;
    private final int chunksX;
    private final int chunksZ;
    private final int scale;
    private final int width;
    private final int height;
    private final byte[] pixels;
    private final int[] present;
    private final long[] counts = new long[STATUSES];

    public GenerationMap(final Selection selection) {
        this.selection = selection;
        this.shape = ShapeFactory.getShape(selection);
        this.minChunkX = selection.centerChunkX() - selection.radiusChunksX();
        this.minChunkZ = selection.centerChunkZ() - selection.radiusChunksZ();
        this.chunksX = 2 * selection.radiusChunksX() + 1;
        this.chunksZ = 2 * selection.radiusChunksZ() + 1;
        this.scale = Math.ceilDiv(Math.max(chunksX, chunksZ), MAX_SIZE);
        this.width = Math.ceilDiv(chunksX, scale);
        this.height = Math.ceilDiv(chunksZ, scale);
        this.pixels = new byte[width * height];
        if (scale == 1) {
            this.present = null;
            Arrays.fill(pixels, (byte) MISSING);
        } else {
            this.present = new int[width * height];
        }
    }

    public void scan() throws IOException {
        final Optional<Path> regionDirectory = selection.world().getRegionDirectory();
        if (regionDirectory.isPresent() && Files.isDirectory(regionDirectory.get())) {
            final int minRegionX = minChunkX >> 5;
            final int minRegionZ = minChunkZ >> 5;
            final int maxRegionX = (minChunkX + chunksX - 1) >> 5;
            final int maxRegionZ = (minChunkZ + chunksZ - 1) >> 5;
            final List<Path> regions;
            try (final Stream<Path> files = Files.list(regionDirectory.get())) {
                regions = files.filter(file -> ChunkCoordinate.fromRegionFile(file.getFileName().toString())
                                .filter(region -> region.x() >= minRegionX && region.x() <= maxRegionX && region.z() >= minRegionZ && region.z() <= maxRegionZ)
                                .isPresent())
                        .toList();
            }
            final AtomicInteger threadCount = new AtomicInteger();
            final ExecutorService executor = Executors.newFixedThreadPool(THREADS, runnable -> {
                final Thread thread = new Thread(runnable, "Chunky Generation Map - %s #%d".formatted(selection.world().getName(), threadCount.incrementAndGet()));
                thread.setDaemon(true);
                return thread;
            });
            try {
                final List<Future<?>> futures = new ArrayList<>();
                for (final Path region : regions) {
                    futures.add(executor.submit(() -> scanRegion(region)));
                }
                for (final Future<?> future : futures) {
                    future.get();
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted", e);
            } catch (final ExecutionException e) {
                throw new IOException(e.getCause());
            } finally {
                executor.shutdownNow();
            }
        }
        countMissing();
    }

    private void scanRegion(final Path region) {
        final ChunkCoordinate regionCoordinate = ChunkCoordinate.fromRegionFile(region.getFileName().toString()).orElseThrow();
        final int regionChunkX = regionCoordinate.x() << 5;
        final int regionChunkZ = regionCoordinate.z() << 5;
        final RegionFile regionFile = new RegionFile(region.toFile(), FILTER);
        final int[] statuses = new int[1024];
        for (int i = 0; i < 1024; ++i) {
            final int x = regionChunkX + (i & 31);
            final int z = regionChunkZ + (i >> 5);
            if (regionFile.isInvalid(x, z)) {
                statuses[i] = INVALID;
            } else {
                statuses[i] = regionFile.getChunk(x, z).map(chunk -> classify(chunk, x, z)).orElse(MISSING);
            }
        }
        record(regionChunkX, regionChunkZ, statuses);
    }

    private int classify(final Chunk chunk, final int x, final int z) {
        if (!(chunk.getData() instanceof final CompoundTag data) || !RegionStatusReader.isValid(data, x, z)) {
            return INVALID;
        }
        if (!RegionStatusReader.isFullStatus(data)) {
            return INCOMPLETE;
        }
        return RegionStatusReader.isFull(data) ? FULL : UNFINISHED;
    }

    private synchronized void record(final int regionChunkX, final int regionChunkZ, final int[] statuses) {
        for (int i = 0; i < 1024; ++i) {
            final int status = statuses[i];
            if (status == MISSING) {
                continue;
            }
            final int x = regionChunkX + (i & 31);
            final int z = regionChunkZ + (i >> 5);
            final int offsetX = x - minChunkX;
            final int offsetZ = z - minChunkZ;
            if (offsetX < 0 || offsetZ < 0 || offsetX >= chunksX || offsetZ >= chunksZ) {
                continue;
            }
            if (shape.isBounding((x << 4) + 8, (z << 4) + 8)) {
                ++counts[status];
            }
            final int pixel = (offsetZ / scale) * width + offsetX / scale;
            if (present == null) {
                pixels[pixel] = (byte) status;
            } else {
                pixels[pixel] = (byte) Math.max(pixels[pixel], status);
                ++present[pixel];
            }
        }
    }

    private void countMissing() {
        for (int pixelZ = 0; pixelZ < height; ++pixelZ) {
            final int coveredZ = Math.min(scale, chunksZ - pixelZ * scale);
            for (int pixelX = 0; pixelX < width; ++pixelX) {
                final int pixel = pixelZ * width + pixelX;
                final int coveredX = Math.min(scale, chunksX - pixelX * scale);
                final int missing = present == null ? (pixels[pixel] == MISSING ? 1 : 0) : coveredX * coveredZ - present[pixel];
                if (missing <= 0) {
                    continue;
                }
                pixels[pixel] = (byte) Math.max(pixels[pixel], MISSING);
                if (isInside(pixelX, pixelZ)) {
                    counts[MISSING] += missing;
                }
            }
        }
    }

    private boolean isInside(final int pixelX, final int pixelZ) {
        final int chunkX = minChunkX + pixelX * scale + Math.min(scale, chunksX - pixelX * scale) / 2;
        final int chunkZ = minChunkZ + pixelZ * scale + Math.min(scale, chunksZ - pixelZ * scale) / 2;
        return shape.isBounding((chunkX << 4) + 8, (chunkZ << 4) + 8);
    }

    public long getCount(final int status) {
        return counts[status];
    }

    public boolean isExact() {
        return scale == 1;
    }

    public void write(final Path path) throws IOException {
        final Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        final byte[] red = new byte[COLORS.length];
        final byte[] green = new byte[COLORS.length];
        final byte[] blue = new byte[COLORS.length];
        for (int i = 0; i < COLORS.length; ++i) {
            red[i] = (byte) (COLORS[i] >> 16);
            green[i] = (byte) (COLORS[i] >> 8);
            blue[i] = (byte) COLORS[i];
        }
        final IndexColorModel colors = new IndexColorModel(4, COLORS.length, red, green, blue, OUTSIDE);
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY, colors);
        for (int pixelZ = 0; pixelZ < height; ++pixelZ) {
            for (int pixelX = 0; pixelX < width; ++pixelX) {
                final int status = pixels[pixelZ * width + pixelX];
                image.getRaster().setSample(pixelX, pixelZ, 0, isInside(pixelX, pixelZ) ? status : OUTSIDE);
            }
        }
        if (!ImageIO.write(image, "png", path.toFile())) {
            throw new IOException("No PNG writer available");
        }
    }
}
