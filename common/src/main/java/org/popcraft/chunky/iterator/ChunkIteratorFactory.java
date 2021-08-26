package org.popcraft.chunky.iterator;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.shape.ShapeType;

public class ChunkIteratorFactory {
    public static ChunkIterator getChunkIterator(Selection selection, long count) {
        switch (selection.shape()) {
            case ShapeType.RECTANGLE:
            case ShapeType.ELLIPSE:
            case ShapeType.OVAL:
                return new Loop2ChunkIterator(selection, count);
            default:
                break;
        }
        switch (selection.pattern()) {
            case PatternType.LOOP:
                return new Loop2ChunkIterator(selection, count);
            case PatternType.SPIRAL:
                return new SpiralChunkIterator(selection, count);
            case PatternType.CONCENTRIC:
            default:
                return new ConcentricChunkIterator(selection, count);
        }
    }

    public static ChunkIterator getChunkIterator(Selection selection) {
        return getChunkIterator(selection, 0);
    }
}
