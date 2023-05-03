package org.popcraft.chunky.nbt.util;

import org.popcraft.chunky.nbt.CompoundTag;
import org.popcraft.chunky.nbt.IntTag;
import org.popcraft.chunky.nbt.Tag;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.zip.InflaterInputStream;

public final class RegionFile {
    private static final int ENTRIES = 1024;
    private static final int SECTOR_SIZE = 4096;
    private final Set<Chunk> chunks = new HashSet<>();
    private final Map<ChunkPos, Chunk> chunkMap = new HashMap<>();

    public RegionFile(final File file) {
        try (final RandomAccessFile region = new RandomAccessFile(file, "r")) {
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
            for (int i = 0; i < ENTRIES; ++i) {
                final int offset = offsetTable[i] * SECTOR_SIZE;
                final int size = sizeTable[i] * SECTOR_SIZE;
                if (offset == 0 && size == 0) {
                    continue;
                }
                region.seek(offset);
                final int length = region.readInt();
                final byte compressionType = region.readByte();
                if (compressionType != 2) {
                    throw new UnsupportedOperationException("Not in zlib format");
                }
                final byte[] compressed = new byte[length - 1];
                region.readFully(compressed);
                final DataInputStream input = new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(compressed)));
                if (Tag.load(input) instanceof final CompoundTag data) {
                    chunks.add(new Chunk(data, timestampTable[i]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (final Chunk chunk : chunks) {
            final CompoundTag data = chunk.getData();
            final Optional<IntTag> xPos = data.getInt("xPos");
            final Optional<IntTag> zPos = data.getInt("zPos");
            if (xPos.isPresent() && zPos.isPresent()) {
                final int x = xPos.get().value();
                final int z = zPos.get().value();
                chunkMap.put(ChunkPos.of(x, z), chunk);
            }
        }
    }

    public Collection<Chunk> getChunks() {
        return chunks;
    }

    public Optional<Chunk> getChunk(final int x, final int z) {
        return Optional.ofNullable(chunkMap.get(ChunkPos.of(x, z)));
    }
}
