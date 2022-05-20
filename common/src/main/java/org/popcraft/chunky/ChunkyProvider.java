package org.popcraft.chunky;

public final class ChunkyProvider {
    private static Chunky instance;

    private ChunkyProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

    public static Chunky get() {
        if (instance == null) {
            throw new IllegalStateException("Chunky is not loaded.");
        }
        return instance;
    }

    static void register(final Chunky instance) {
        ChunkyProvider.instance = instance;
    }

    static void unregister() {
        ChunkyProvider.instance = null;
    }
}
