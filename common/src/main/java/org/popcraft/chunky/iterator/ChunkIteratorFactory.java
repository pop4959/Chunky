package org.popcraft.chunky.iterator;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.shape.ShapeType;

public final class ChunkIteratorFactory {
    private ChunkIteratorFactory() {
    }

    public static ChunkIterator getChunkIterator(final Selection selection, final long count) {
        final String shape = selection.shape();
        if (ShapeType.RECTANGLE.equals(shape) || ShapeType.ELLIPSE.equals(shape) || ShapeType.OVAL.equals(shape)) {
            return new Loop2ChunkIterator(selection, count);
        }
        return switch (selection.pattern().getType()) {
            case PatternType.LOOP -> new Loop2ChunkIterator(selection, count);
            case PatternType.SPIRAL -> new SpiralChunkIterator(selection, count);
            case PatternType.CSV -> new CsvChunkIterator(selection, count);
            default -> new ConcentricChunkIterator(selection, count);
        };
    }

    public static ChunkIterator getChunkIterator(final Selection selection) {
        return getChunkIterator(selection, 0);
    }
}
