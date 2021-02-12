package org.popcraft.chunky.util;

public class Formatting {
    private static char[] BINARY_PREFIXES = new char[]{'K', 'M', 'G', 'T', 'P'};

    public static String bytes(long bytes) {
        long value = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (value < 1024) {
            return String.format("%d B", bytes);
        }
        int i = BINARY_PREFIXES.length - 1;
        long prefixValue = 1L << (BINARY_PREFIXES.length * 10);
        for (; i > 0; --i) {
            if (value >= prefixValue) {
                break;
            }
            prefixValue >>= 10;
        }
        return String.format("%.1f %cB", bytes / (double) prefixValue, BINARY_PREFIXES[i]);
    }
}
