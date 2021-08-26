package org.popcraft.chunky.util;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.shape.ShapeType;

import java.text.DecimalFormat;

public class Formatting {
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#.##");
    private static final char[] BINARY_PREFIXES = new char[]{'K', 'M', 'G', 'T', 'P'};

    private Formatting() {
    }

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

    public static String radius(Selection selection) {
        if (ShapeType.RECTANGLE.equals(selection.shape()) || ShapeType.ELLIPSE.equals(selection.shape())) {
            return String.format("%s, %s", number(selection.radiusX()), number(selection.radiusZ()));
        } else {
            return String.format("%s", number(selection.radiusX()));
        }
    }

    public static synchronized String number(double number) {
        return NUMBER_FORMAT.format(number);
    }
}
