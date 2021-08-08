package org.popcraft.chunky.util;

public final class Pair<L, R> {
    private final L left;
    private final R right;

    private Pair(final L left, final R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Pair<L, R> of(final L left, final R right) {
        return new Pair<>(left, right);
    }

    public L left() {
        return this.left;
    }

    public R right() {
        return this.right;
    }
}
