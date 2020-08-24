package org.popcraft.chunky.iterator;

public class ChunkIteratorFactory {
    public static ChunkIterator getChunkIterator(String type, int radius, int xCenter, int zCenter, long count) {
        switch (type) {
            case "loop":
                return new Loop2ChunkIterator(radius, xCenter, zCenter, count);
            case "spiral":
                return new SpiralChunkIterator(radius, xCenter, zCenter, count);
            case "concentric":
            default:
                return new ConcentricChunkIterator(radius, xCenter, zCenter, count);
        }
    }

    public static ChunkIterator getChunkIterator(String type, int radius, int xCenter, int zCenter) {
        return getChunkIterator(type, radius, xCenter, zCenter, 0);
    }
}
