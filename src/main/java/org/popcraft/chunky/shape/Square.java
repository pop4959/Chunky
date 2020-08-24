package org.popcraft.chunky.shape;

import org.popcraft.chunky.ChunkCoordinate;

public class Square implements Shape {
    @Override
    public boolean isBounding(ChunkCoordinate chunkCoordinate) {
        return true;
    }

    @Override
    public String name() {
        return "square";
    }
}
