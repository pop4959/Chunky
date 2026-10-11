package org.popcraft.chunky.iterator;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.shape.ShapeType;
import org.popcraft.chunky.util.ChunkCoordinate;
import org.popcraft.chunky.util.GenerationSettings;
import org.popcraft.chunky.util.Parameter;

import java.util.NoSuchElementException;

/** Traverses short X rows within each strip before advancing to the next strip. */
public class StripChunkIterator implements ChunkIterator {
    private final int minX, minZ, width;
    private final long sizeX, sizeZ, total;
    private long current;

    public StripChunkIterator(final Selection selection, final long count) {
        this(selection);
        this.current = Math.max(0, count);
    }

    public StripChunkIterator(final Selection selection) {
        final int radiusX = selection.radiusChunksX();
        final String shape = selection.shape();
        final int radiusZ = ShapeType.RECTANGLE.equals(shape) || ShapeType.ELLIPSE.equals(shape) || ShapeType.OVAL.equals(shape)
                ? selection.radiusChunksZ() : radiusX;
        this.minX = selection.centerChunkX() - radiusX;
        this.minZ = selection.centerChunkZ() - radiusZ;
        this.sizeX = 2L * radiusX + 1;
        this.sizeZ = 2L * radiusZ + 1;
        final int maxWidth = selection.pattern().getValue().map(Integer::parseInt)
                .orElseGet(GenerationSettings::maxWorkingCount);
        if (maxWidth < 1) {
            throw new IllegalArgumentException("Strip width must be positive");
        }
        this.width = (int) Math.min(sizeX, maxWidth);
        this.total = sizeX * sizeZ;
    }

    @Override
    public boolean hasNext() {
        return current < total;
    }

    @Override
    public ChunkCoordinate next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        final long stripSize = width * sizeZ;
        final long stripX = current / stripSize * width;
        final long offset = current % stripSize;
        final long stripWidth = Math.min(width, sizeX - stripX);
        ++current;
        return new ChunkCoordinate((int) (minX + stripX + offset % stripWidth),
                (int) (minZ + offset / stripWidth));
    }

    @Override
    public long total() {
        return total;
    }

    @Override
    public String name() {
        return Parameter.of(PatternType.STRIP, String.valueOf(width)).toString();
    }
}
