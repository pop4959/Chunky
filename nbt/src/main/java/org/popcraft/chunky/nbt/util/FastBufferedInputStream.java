package org.popcraft.chunky.nbt.util;

import java.io.IOException;
import java.io.InputStream;

final class FastBufferedInputStream extends InputStream {
    private static final int BUFFER_SIZE = 8192;
    private final InputStream in;
    private final byte[] buffer = new byte[BUFFER_SIZE];
    private int position;
    private int limit;

    FastBufferedInputStream(final InputStream in) {
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        if (position >= limit && !fill()) {
            return -1;
        }
        return buffer[position++] & 0xFF;
    }

    @Override
    public int read(final byte[] bytes, final int offset, final int length) throws IOException {
        if (length == 0) {
            return 0;
        }
        int available = limit - position;
        if (available <= 0) {
            if (length >= BUFFER_SIZE) {
                return in.read(bytes, offset, length);
            }
            if (!fill()) {
                return -1;
            }
            available = limit - position;
        }
        final int read = Math.min(available, length);
        System.arraycopy(buffer, position, bytes, offset, read);
        position += read;
        return read;
    }

    @Override
    public long skip(final long count) throws IOException {
        if (count <= 0) {
            return 0;
        }
        if (position >= limit && !fill()) {
            return 0;
        }
        final int skipped = (int) Math.min(limit - position, count);
        position += skipped;
        return skipped;
    }

    @Override
    public int available() throws IOException {
        return (limit - position) + in.available();
    }

    @Override
    public void close() throws IOException {
        in.close();
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
