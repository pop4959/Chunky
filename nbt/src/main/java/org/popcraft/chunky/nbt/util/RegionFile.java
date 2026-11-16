package org.popcraft.chunky.nbt.util;

import org.popcraft.chunky.nbt.Tag;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public final class RegionFile {
    private static final int ENTRIES = 1024;
    private static final int SECTOR_SIZE = 4096;
    private static final ThreadLocal<Inflater> INFLATER = ThreadLocal.withInitial(Inflater::new);
    private final Chunk[] chunks = new Chunk[ENTRIES];
    private final BitSet invalidChunks = new BitSet(ENTRIES);
    private int regionX;
    private int regionZ;
    private final Inflater inflater = INFLATER.get();
    private Header header;
    private boolean fullyRead;

    public RegionFile(final File file) {
        this(file, null);
    }

    public RegionFile(final File file, final ChunkFilter filter) {
        this(file, filter, 0, 1);
    }

    public RegionFile(final File file, final ChunkFilter filter, final int part, final int parts) {
        this(file, filter, part, parts, false);
    }

    public RegionFile(final File file, final boolean headerOnly) {
        this(file, null, 0, 1, headerOnly);
    }

    private RegionFile(final File file, final ChunkFilter filter, final int part, final int parts, final boolean headerOnly) {
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
            this.regionX = regionX;
            this.regionZ = regionZ;
            final byte[] headerBytes = new byte[ENTRIES * 8];
            region.readFully(headerBytes);
            final IntBuffer headerInts = ByteBuffer.wrap(headerBytes).asIntBuffer();
            final int[] locationTable = new int[ENTRIES];
            headerInts.get(locationTable);
            final int[] timestampTable = new int[ENTRIES];
            headerInts.get(timestampTable);
            header = new Header(locationTable, timestampTable);
            if (headerOnly) {
                fullyRead = true;
                return;
            }
            final FileChannel channel = region.getChannel();
            byte[] chunkBuffer = new byte[65536];
            final BufferedDataInput input = new BufferedDataInput();
            final int[] offsetTable = new int[ENTRIES];
            final int[] sizeTable = new int[ENTRIES];
            for (int i = 0; i < ENTRIES; ++i) {
                offsetTable[i] = (locationTable[i] >> 8) & 0xFFFFFF;
                sizeTable[i] = locationTable[i] & 0xFF;
            }
            final long[] chunksByOffset = new long[ENTRIES];
            for (int i = 0; i < ENTRIES; ++i) {
                chunksByOffset[i] = (long) offsetTable[i] * ENTRIES + i;
            }
            Arrays.sort(chunksByOffset);
            boolean complete = true;
            for (int position = part; position < ENTRIES; position += parts) {
                final int i = (int) (chunksByOffset[position] % ENTRIES);
                final int offset = offsetTable[i] * SECTOR_SIZE;
                final int size = sizeTable[i] * SECTOR_SIZE;
                if (offset == 0 && size == 0) {
                    continue;
                }
                final int x = (regionX * 32) + (i % 32);
                final int z = (regionZ * 32) + (i / 32);
                try {
                    int buffered = channel.read(ByteBuffer.wrap(chunkBuffer, 0, Math.min(size, chunkBuffer.length)), offset);
                    if (buffered < 5) {
                        throw new IOException("Truncated chunk %d, %d in %s".formatted(x, z, file.getName()));
                    }
                    final int length = ByteBuffer.wrap(chunkBuffer).getInt(0);
                    final int compressionType = chunkBuffer[4] & 0xFF;
                    final byte[] compressed;
                    final int compressedOffset;
                    final int compressedLength;
                    if ((compressionType & CompressionType.EXTERNAL_FLAG) != 0) {
                        final Path externalFile = file.toPath().getParent().resolve("c.%d.%d.mcc".formatted(x, z));
                        if (Files.notExists(externalFile)) {
                            invalidChunks.set(i);
                            continue;
                        }
                        compressed = Files.readAllBytes(externalFile);
                        compressedOffset = 0;
                        compressedLength = compressed.length;
                    } else {
                        if (length <= 1 || length > size - 4) {
                            throw new IOException("Invalid length %d for chunk %d, %d in %s".formatted(length, x, z, file.getName()));
                        }
                        if (chunkBuffer.length < length + 4) {
                            chunkBuffer = Arrays.copyOf(chunkBuffer, length + 4);
                        }
                        if (buffered < length + 4) {
                            buffered += channel.read(ByteBuffer.wrap(chunkBuffer, buffered, length + 4 - buffered), offset + buffered);
                            if (buffered < length + 4) {
                                throw new IOException("Truncated chunk %d, %d in %s".formatted(x, z, file.getName()));
                            }
                        }
                        compressed = chunkBuffer;
                        compressedOffset = 5;
                        compressedLength = length - 1;
                    }
                    try (final ByteArrayInputStream bytes = new ByteArrayInputStream(compressed, compressedOffset, compressedLength);
                         final InputStream decompressed = decompress(bytes, compressionType & ~CompressionType.EXTERNAL_FLAG)) {
                        input.reset(decompressed);
                        final Tag data;
                        if (filter == null) {
                            data = Tag.load(input);
                        } else {
                            data = filter.hasTagNames() ?
                                    Tag.findMany(input, filter.getTagNames(), filter.getStop()) :
                                    Tag.find(input, filter.getType(), filter.getName());
                        }
                        chunks[i] = new Chunk(x, z, data, timestampTable[i]);
                    }
                } catch (final IOException | RuntimeException ignored) {
                    invalidChunks.set(i);
                    complete = false;
                }
            }
            fullyRead = complete;
        } catch (final IOException ignored) {
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
        final List<Chunk> list = new ArrayList<>();
        for (final Chunk chunk : chunks) {
            if (chunk != null) {
                list.add(chunk);
            }
        }
        return list;
    }

    public Optional<Chunk> getChunk(final int x, final int z) {
        final Chunk chunk = chunks[(x & 31) + (z & 31) * 32];
        return chunk != null && chunk.getX() == x && chunk.getZ() == z ? Optional.of(chunk) : Optional.empty();
    }

    public Optional<Header> getHeader() {
        return Optional.ofNullable(header);
    }

    public boolean isInvalid(final int x, final int z) {
        return (x >> 5) == regionX && (z >> 5) == regionZ && invalidChunks.get((x & 31) + (z & 31) * 32);
    }

    public boolean isFullyRead() {
        return fullyRead;
    }

    public record Header(int[] locations, int[] timestamps) {
        public boolean hasChunk(final int x, final int z) {
            return locations[index(x, z)] != 0;
        }

        public long getTimestamp(final int x, final int z) {
            return Integer.toUnsignedLong(timestamps[index(x, z)]);
        }

        private static int index(final int x, final int z) {
            return (x & 31) + (z & 31) * 32;
        }
    }
}
