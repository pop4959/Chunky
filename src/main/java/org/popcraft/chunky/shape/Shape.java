package org.popcraft.chunky.shape;

import org.popcraft.chunky.ChunkCoordinate;

@FunctionalInterface
public interface Shape {
    boolean isBounding(ChunkCoordinate chunkCoordinate);

    default String name() {
        return "shape";
    }
}
