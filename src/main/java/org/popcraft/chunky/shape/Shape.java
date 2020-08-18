package org.popcraft.chunky.shape;

import org.popcraft.chunky.ChunkCoordinate;

public interface Shape {
    boolean isBounding(ChunkCoordinate chunkCoordinate);
}
