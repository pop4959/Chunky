package org.popcraft.chunky.iterator;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.shape.ShapeType;

public final class ChunkIteratorFactory {
    private ChunkIteratorFactory() {
    }

    public static ChunkIterator getChunkIterator(final Selection selection, final long count) {
        switch (selection.shape()) {
            case ShapeType.RECTANGLE:
            case ShapeType.ELLIPSE:
            case ShapeType.OVAL:
                return new Loop2ChunkIterator(selection, count);
            default:
                break;
        }
        switch (selection.pattern().getType()) {
            case PatternType.LOOP:
                return new Loop2ChunkIterator(selection, count);
            case PatternType.SPIRAL:
                return new SpiralChunkIterator(selection, count);
            case PatternType.CSV:
                return new CsvChunkIterator(selection, count);
            case PatternType.CONCENTRIC:
            default:
                return new ConcentricChunkIterator(selection, count);
        }
    }

    public static ChunkIterator getChunkIterator(final Selection selection) {
        return getChunkIterator(selection, 0);
    }
}
