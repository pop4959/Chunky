package org.popcraft.chunky.nbt.util;

import net.jpountz.lz4.LZ4BlockInputStream;

import java.io.InputStream;

final class Lz4Decompressor {
    private Lz4Decompressor() {
    }

    static InputStream decompress(final InputStream input) {
        return new LZ4BlockInputStream(input);
    }
}
