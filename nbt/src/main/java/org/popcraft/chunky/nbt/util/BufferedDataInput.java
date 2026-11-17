package org.popcraft.chunky.nbt.util;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public final class BufferedDataInput implements DataInput {
    private static final int BUFFER_SIZE = 8192;
    private static final String[] ASCII = new String[128];
    private static final ValueLayout.OfInt INT_BE = ValueLayout.JAVA_INT_UNALIGNED.withOrder(ByteOrder.BIG_ENDIAN);
    private static final ValueLayout.OfLong LONG_BE = ValueLayout.JAVA_LONG_UNALIGNED.withOrder(ByteOrder.BIG_ENDIAN);
    private final byte[] buffer = new byte[BUFFER_SIZE];
    private final ByteBuffer view = ByteBuffer.wrap(buffer);
    private final MemorySegment segment = MemorySegment.ofArray(buffer);
    private InputStream in;
    private int position;
    private int limit;

    static {
        for (int i = 0; i < ASCII.length; ++i) {
            ASCII[i] = String.valueOf((char) i);
        }
    }

    public BufferedDataInput() {
    }

    public BufferedDataInput(final InputStream in) {
        this.in = in;
    }

    public void reset(final InputStream in) {
        this.in = in;
        this.position = 0;
        this.limit = 0;
    }

    @Override
    public void readFully(final byte[] bytes) throws IOException {
        readFully(bytes, 0, bytes.length);
    }

    @Override
    public void readFully(final byte[] bytes, final int offset, final int length) throws IOException {
        final int buffered = Math.min(limit - position, length);
        System.arraycopy(buffer, position, bytes, offset, buffered);
        position += buffered;
        int read = buffered;
        while (read < length) {
            final int count = in.read(bytes, offset + read, length - read);
            if (count < 0) {
                throw new EOFException();
            }
            read += count;
        }
    }

    @Override
    public int skipBytes(final int count) throws IOException {
        int skipped = Math.min(limit - position, Math.max(count, 0));
        position += skipped;
        while (skipped < count) {
            if (!fill()) {
                break;
            }
            final int step = Math.min(limit - position, count - skipped);
            position += step;
            skipped += step;
        }
        return skipped;
    }

    @Override
    public boolean readBoolean() throws IOException {
        return readByte() != 0;
    }

    @Override
    public byte readByte() throws IOException {
        if (position >= limit && !fill()) {
            throw new EOFException();
        }
        return buffer[position++];
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return readByte() & 0xFF;
    }

    @Override
    public short readShort() throws IOException {
        ensure(2);
        final short value = view.getShort(position);
        position += 2;
        return value;
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return readShort() & 0xFFFF;
    }

    @Override
    public char readChar() throws IOException {
        return (char) readShort();
    }

    @Override
    public int readInt() throws IOException {
        ensure(4);
        final int value = view.getInt(position);
        position += 4;
        return value;
    }

    @Override
    public long readLong() throws IOException {
        ensure(8);
        final long value = view.getLong(position);
        position += 8;
        return value;
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    public String readLine() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String readUTF() throws IOException {
        ensure(2);
        final int length = view.getShort(position) & 0xFFFF;
        if (length <= BUFFER_SIZE - 2) {
            ensure(2 + length);
            final int start = position + 2;
            boolean ascii = true;
            for (int i = start; i < start + length; ++i) {
                if (buffer[i] < 0) {
                    ascii = false;
                    break;
                }
            }
            if (ascii) {
                position = start + length;
                if (length == 0) {
                    return "";
                }
                if (length == 1) {
                    return ASCII[buffer[start]];
                }
                return new String(buffer, start, length, StandardCharsets.ISO_8859_1);
            }
        }
        return DataInputStream.readUTF(this);
    }

    public void readInts(final int[] values) throws IOException {
        int index = 0;
        while (index < values.length) {
            if (limit - position < 4) {
                ensure(4);
            }
            final int count = Math.min(values.length - index, (limit - position) >> 2);
            MemorySegment.copy(segment, INT_BE, position, values, index, count);
            position += count << 2;
            index += count;
        }
    }

    public void readLongs(final long[] values) throws IOException {
        int index = 0;
        while (index < values.length) {
            if (limit - position < 8) {
                ensure(8);
            }
            final int count = Math.min(values.length - index, (limit - position) >> 3);
            MemorySegment.copy(segment, LONG_BE, position, values, index, count);
            position += count << 3;
            index += count;
        }
    }

    private void ensure(final int count) throws IOException {
        if (limit - position >= count) {
            return;
        }
        final int remaining = limit - position;
        System.arraycopy(buffer, position, buffer, 0, remaining);
        position = 0;
        limit = remaining;
        while (limit < count) {
            final int read = in.read(buffer, limit, BUFFER_SIZE - limit);
            if (read < 0) {
                throw new EOFException();
            }
            limit += read;
        }
    }

    private boolean fill() throws IOException {
        final int read = in.read(buffer, 0, BUFFER_SIZE);
        if (read <= 0) {
            position = 0;
            limit = 0;
            return false;
        }
        position = 0;
        limit = read;
        return true;
    }
}
