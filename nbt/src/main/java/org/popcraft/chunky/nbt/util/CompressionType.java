package org.popcraft.chunky.nbt.util;

public final class CompressionType {
    public static final int GZIP = 1;
    public static final int ZLIB = 2;
    public static final int NONE = 3;
    public static final int LZ4 = 4;
    public static final int EXTERNAL_FLAG = 128;

    private CompressionType() {
    }
}
