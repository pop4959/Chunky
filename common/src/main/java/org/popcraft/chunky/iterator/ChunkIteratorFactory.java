package org.popcraft.chunky.iterator;

import org.popcraft.chunky.Selection;

public class ChunkIteratorFactory {
    public static ChunkIterator getChunkIterator(Selection selection, long count) {
        switch (selection.shape) {
            case "rectangle":
            case "oval":
                return new Loop2ChunkIterator(selection, count);
            default:
                break;
        }
        switch (selection.pattern) {
            case "loop":
                return new Loop2ChunkIterator(selection, count);
            case "spiral":
                return new SpiralChunkIterator(selection, count);
            case "concentric":
            default:
                return new ConcentricChunkIterator(selection, count);
        }
    }

    public static ChunkIterator getChunkIterator(Selection selection) {
        return getChunkIterator(selection, 0);
    }
}
