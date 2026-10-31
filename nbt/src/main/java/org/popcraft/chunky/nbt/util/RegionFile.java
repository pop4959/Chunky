package org.popcraft.chunky.nbt.util;

import org.popcraft.chunky.nbt.Tag;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public final class RegionFile {
    private static final int ENTRIES = 1024;
    private static final int SECTOR_SIZE = 4096;
    private final Set<Chunk> chunks = new HashSet<>();
    private final Map<ChunkPos, Chunk> chunkMap = new HashMap<>();
    private final Inflater inflater = new Inflater();
    private boolean fullyRead;

    public RegionFile(final File file) {
        this(file, null);
    }

    public RegionFile(final File file, final ChunkFilter filter) {
        try (final RandomAccessFile region = new RandomAccessFile(file, "r")) {
            if (region.length() < 4096) {
                return;
            }
            final String regionFileName = file.getName();
            if (!regionFileName.startsWith("r.")) {
                return;
            }
            final int extension = regionFileName.indexOf(".mca");
            if (extension < 2) {
                return;
            }
            final String regionCoordinates = regionFileName.substring(2, extension);
            final int separator = regionCoordinates.indexOf('.');
            final int regionX;
            final int regionZ;
            try {
                regionX = Integer.parseInt(regionCoordinates.substring(0, separator));
                regionZ = Integer.parseInt(regionCoordinates.substring(separator + 1));
            } catch (final NumberFormatException e) {
                return;
            }
            final int[] offsetTable = new int[ENTRIES];
            final int[] sizeTable = new int[ENTRIES];
            for (int i = 0; i < ENTRIES; ++i) {
                final int location = region.readInt();
                offsetTable[i] = (location >> 8) & 0xFFFFFF;
                sizeTable[i] = location & 0xFF;
            }
            final int[] timestampTable = new int[ENTRIES];
            for (int i = 0; i < ENTRIES; ++i) {
                timestampTable[i] = region.readInt();
            }
            final long[] chunksByOffset = new long[ENTRIES];
            for (int i = 0; i < ENTRIES; ++i) {
                chunksByOffset[i] = (long) offsetTable[i] * ENTRIES + i;
            }
            Arrays.sort(chunksByOffset);
            for (final long entry : chunksByOffset) {
                final int i = (int) (entry % ENTRIES);
                final int offset = offsetTable[i] * SECTOR_SIZE;
                final int size = sizeTable[i] * SECTOR_SIZE;
                if (offset == 0 && size == 0) {
                    continue;
                }
                final int x = (regionX * 32) + (i % 32);
                final int z = (regionZ * 32) + (i / 32);
                region.seek(offset);
                final int length = region.readInt();
                final int compressionType = region.readByte() & 0xFF;
                final byte[] compressed;
                if ((compressionType & CompressionType.EXTERNAL_FLAG) != 0) {
                    final Path externalFile = file.toPath().getParent().resolve("c.%d.%d.mcc".formatted(x, z));
                    if (Files.notExists(externalFile)) {
                        continue;
                    }
                    compressed = Files.readAllBytes(externalFile);
                } else {
                    if (length <= 1 || length > size - 4) {
                        throw new IOException("Invalid length %d for chunk %d, %d in %s".formatted(length, x, z, file.getName()));
                    }
                    compressed = new byte[length - 1];
                    region.readFully(compressed);
                }
                try (final ByteArrayInputStream bytes = new ByteArrayInputStream(compressed);
                     final InputStream decompressed = decompress(bytes, compressionType & ~CompressionType.EXTERNAL_FLAG);
                     final FastBufferedInputStream buffer = new FastBufferedInputStream(decompressed);
                     final DataInputStream input = new DataInputStream(buffer)) {
                    final Tag data;
                    if (filter == null) {
                        data = Tag.load(input);
                    } else {
                        data = filter.hasTagNames() ?
                                Tag.findMany(input, filter.getTagNames()) :
                                Tag.find(input, filter.getType(), filter.getName());
                    }
                    final Chunk chunk = new Chunk(x, z, data, timestampTable[i]);
                    chunks.add(chunk);
                    chunkMap.put(ChunkPos.of(x, z), chunk);
                }
            }
            fullyRead = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            inflater.end();
        }
    }

    private InputStream decompress(final InputStream input, final int compressionType) throws IOException {
        return switch (compressionType) {
            case CompressionType.GZIP -> new GZIPInputStream(input);
            case CompressionType.ZLIB -> {
                inflater.reset();
                yield new InflaterInputStream(input, inflater);
            }
            case CompressionType.NONE -> input;
            case CompressionType.LZ4 -> Lz4Decompressor.decompress(input);
            default -> throw new UnsupportedOperationException("Unsupported compression type %d".formatted(compressionType));
        };
    }

    public Collection<Chunk> getChunks() {
        return chunks;
    }

    public Optional<Chunk> getChunk(final int x, final int z) {
        return Optional.ofNullable(chunkMap.get(ChunkPos.of(x, z)));
    }

    public boolean isFullyRead() {
        return fullyRead;
    }
}
