package org.popcraft.chunky.iterator;

import java.util.List;

public final class PatternType {
    public static final String CONCENTRIC = "concentric";
    public static final String LOOP = "loop";
    public static final String SPIRAL = "spiral";
    public static final String CSV = "csv";

    public static final List<String> ALL = List.of(CONCENTRIC, LOOP, SPIRAL, CSV);

    private PatternType() {
    }
}
