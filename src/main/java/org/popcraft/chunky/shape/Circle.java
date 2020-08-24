package org.popcraft.chunky.shape;

import org.popcraft.chunky.ChunkCoordinate;
import org.popcraft.chunky.iterator.ChunkIterator;

public class Circle extends AbstractShape {
    public Circle(ChunkIterator chunkIterator) {
        super(chunkIterator);
    }

    @Override
    public boolean isBounding(ChunkCoordinate chunkCoordinate) {
        int xChunk = (chunkCoordinate.x << 4) + 8;
        int zChunk = (chunkCoordinate.z << 4) + 8;
        return Math.hypot(xCenter - xChunk, zCenter - zChunk) < radius;
    }

    @Override
    public String name() {
        return "circle";
    }
}
