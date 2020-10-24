package org.popcraft.chunky.iterator;

import org.popcraft.chunky.ChunkCoordinate;
import org.popcraft.chunky.Selection;

public class SpiralChunkIterator implements ChunkIterator {
    private int x, z;
    private int stopX, stopZ;
    private int span = 1, spanCount, spanProgress;
    private int direction;
    private long total;
    private final static int RIGHT = 0, DOWN = 1, LEFT = 2, UP = 3;
    private boolean hasNext = true;

    public SpiralChunkIterator(Selection selection, long count) {
        this(selection);
        if (count <= 0) {
            return;
        }
        int diameterFinished = (int) Math.floor(Math.sqrt(count));
        if (diameterFinished % 2 == 0) {
            --diameterFinished;
        }
        this.span = diameterFinished;
        this.spanCount = 1;
        this.direction = DOWN;
        int radiusFinished = diameterFinished / 2;
        this.x += radiusFinished + 1;
        this.z += radiusFinished;
        long perimeterCount = count - diameterFinished * diameterFinished;
        long spanned;
        spanned = Math.min(span, perimeterCount);
        this.z -= spanned;
        if (spanned == span) {
            ++span;
            spanCount = 0;
            direction = LEFT;
        } else {
            spanProgress += spanned;
        }
        perimeterCount -= spanned;
        spanned = Math.min(span, perimeterCount);
        this.x -= spanned;
        if (spanned == span) {
            spanCount = 1;
            direction = UP;
        } else {
            spanProgress += spanned;
        }
        perimeterCount -= spanned;
        spanned = Math.min(span, perimeterCount);
        this.z += spanned;
        if (spanned == span) {
            ++span;
            spanCount = 0;
            direction = RIGHT;
        } else {
            spanProgress += spanned;
        }
        perimeterCount -= spanned;
        spanned = Math.min(span, perimeterCount);
        this.x += spanned;
        if (spanned == span) {
            spanCount = 1;
            direction = DOWN;
        } else {
            spanProgress += spanned;
        }
        if (x > stopX) {
            hasNext = false;
        }
    }

    public SpiralChunkIterator(Selection selection) {
        int radiusChunks = selection.getRadiusChunksX();
        this.x = selection.getChunkX();
        this.z = selection.getChunkZ();
        this.stopX = x + radiusChunks;
        this.stopZ = z + radiusChunks;
        long diameter = selection.getDiameterChunksX();
        this.total = diameter * diameter;
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public ChunkCoordinate next() {
        final ChunkCoordinate chunkCoord = new ChunkCoordinate(x, z);
        if (x == stopX && z == stopZ) {
            hasNext = false;
        }
        if (spanCount == 2) {
            spanCount = 0;
            ++span;
        }
        switch (direction) {
            case RIGHT:
                x += 1;
                break;
            case DOWN:
                z -= 1;
                break;
            case LEFT:
                x -= 1;
                break;
            case UP:
                z += 1;
                break;
        }
        ++spanProgress;
        if (spanProgress == span) {
            spanProgress = 0;
            ++spanCount;
            direction = direction == UP ? RIGHT : ++direction;
        }
        return chunkCoord;
    }

    @Override
    public long total() {
        return total;
    }

    @Override
    public String name() {
        return "spiral";
    }
}
