package org.popcraft.chunky.iterator;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.shape.ShapeType;
import org.popcraft.chunky.util.ChunkCoordinate;
import org.popcraft.chunky.util.Input;

import java.util.NoSuchElementException;

public class StripChunkIterator implements ChunkIterator {
    private static final int WIDTH = Math.max(1, Input.tryInteger(System.getProperty("chunky.stripWidth")).orElse(64));
    private final int minX, minZ;
    private final long sizeX, sizeZ, total;
    private long current;

    public StripChunkIterator(final Selection selection, final long count) {
        this(selection);
        this.current = Math.max(0, count);
    }

    public StripChunkIterator(final Selection selection) {
        final String shape = selection.shape();
        final boolean useZ = ShapeType.RECTANGLE.equals(shape) || ShapeType.ELLIPSE.equals(shape) || ShapeType.OVAL.equals(shape);
        this.minX = selection.centerChunkX() - selection.radiusChunksX();
        this.minZ = selection.centerChunkZ() - (useZ ? selection.radiusChunksZ() : selection.radiusChunksX());
        this.sizeX = selection.diameterChunksX();
        this.sizeZ = useZ ? selection.diameterChunksZ() : selection.diameterChunksX();
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
        final long stripSize = WIDTH * sizeZ;
        final long stripX = current / stripSize * WIDTH;
        final long offset = current % stripSize;
        final long currentWidth = Math.min(WIDTH, sizeX - stripX);
        ++current;
        return new ChunkCoordinate((int) (minX + stripX + offset % currentWidth),
                (int) (minZ + offset / currentWidth));
    }

    @Override
    public long total() {
        return total;
    }

    @Override
    public String name() {
        return PatternType.STRIP;
    }
}
