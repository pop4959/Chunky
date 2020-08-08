package org.popcraft.chunky;

import java.util.NoSuchElementException;

public class SpiralChunkCoordinateIterator implements ChunkCoordinateIterator {

    private final int diameterChunks;
    private boolean hasNext = true;
    private ChunkCoordinate chunkCoord;

    private int
            posX,
            posZ,
            flip = -1,
            count = 0,
            subCount = 0,
            stretch = 1;
    private boolean xNotZ = true;

    public SpiralChunkCoordinateIterator(int radius, int centerX, int centerZ, int startCount) {
        if (startCount < 0) throw new IllegalArgumentException("Start count must be 1 or greater.");
        diameterChunks = radius / 8 + (radius % 8 != 0 ? 1 : 0);
        posX = centerX >> 4;
        posZ = centerX >> 4;
        // If we are starting at the first position, we are already done.
        if (startCount == 1) return;
        // Our internal count starts at 0. The count that we display starts at 1.
        count = startCount - 1;
        // Stretch is basically the inverse of { aₙ = n²-n+2 } offset by one and floored.
        // This value is useful to us for other things as well though, so we save it as its own thing.
        stretch = (int) (0.5 + Math.sqrt(1d - 4d * (2 - startCount - 1)) / 2d);
        // SubCount...
        final int stretchStart = stretch * stretch - stretch + 2;
        subCount = (startCount - stretchStart) % stretch + 1;
        // Flip is positive when stretch is even, negative when it is odd
        flip = stretch % 2 == 0 ? 1 : -1;
        // xNotY is true for the first half of each stretch interval, false for the second half.
        xNotZ = startCount - stretchStart < stretch;

        if (stretch % 2 != 0) {
            posX += -1 + (stretch - 1) / 2;
            posZ = (stretch - 1) / 2;
        } else {
            posX += -(stretch / 2 - 1);
            posZ += -1 - (stretch / 2 - 1);
        }
        if (xNotZ) {
            posX += flip * (subCount - 1);
        } else {
            posX += flip * (stretch - 1);
            posZ += flip * (subCount);
        }
    }

    public SpiralChunkCoordinateIterator(int radius, int centerX, int centerZ) {
        diameterChunks = radius / 8 + (radius % 8 != 0 ? 1 : 0);
        posX = centerX >> 4;
        posZ = centerX >> 4;
    }

    @Override
    public ChunkCoordinate next() {
        if (!hasNext()) throw new NoSuchElementException();
        chunkCoord = new ChunkCoordinate(posX, posZ);
        count++;
        if (xNotZ) {
            if (subCount++ < stretch)
                posX += flip;
            else {
                posZ += flip;
                subCount = 1;
                xNotZ = false;
            }
        } else {
            if (subCount++ < stretch)
                posZ += flip;
            else {
                posX += flip *= -1;
                subCount = 1;
                stretch++;
                xNotZ = true;
            }
        }
        return chunkCoord;
    }

    @Override
    public boolean hasNext() {
        if (hasNext && count >= diameterChunks * diameterChunks) hasNext = false;
        return hasNext;
    }

    @Override
    public ChunkCoordinate peek() {
        return chunkCoord;
    }

    @Override
    public long count() {
        return diameterChunks * diameterChunks;
    }

    @Override
    public long covered() {
        return count;
    }
}
