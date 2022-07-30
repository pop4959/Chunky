package org.popcraft.chunky.nbt;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class NBT {
    private NBT() {
    }

    public static Tag load(final InputStream inputStream) throws IOException {
        final DataInput dataInput = new DataInputStream(inputStream);
        return Tag.load(dataInput);
    }

    public static void save(final OutputStream outputStream, final Tag tag) throws IOException {
        final DataOutput dataOutput = new DataOutputStream(outputStream);
        Tag.save(dataOutput, tag);
    }

    public static Tag loadCompressed(final InputStream inputStream) throws IOException {
        try (final GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream)) {
            final DataInput dataInput = new DataInputStream(gzipInputStream);
            return Tag.load(dataInput);
        }
    }

    public static void saveCompressed(final OutputStream outputStream, final Tag tag) throws IOException {
        try (final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
            final DataOutput dataOutput = new DataOutputStream(gzipOutputStream);
            Tag.save(dataOutput, tag);
        }
    }
}
