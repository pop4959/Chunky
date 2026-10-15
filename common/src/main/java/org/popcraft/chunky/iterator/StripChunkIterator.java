package org.popcraft.chunky.iterator;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.shape.ShapeType;
import org.popcraft.chunky.util.ChunkCoordinate;
import org.popcraft.chunky.util.GenerationSettings;
import org.popcraft.chunky.util.Parameter;

import java.util.NoSuchElementException;

/** Interleaves full-height strips from the center X outward, starting at minimum Z. */
public class StripChunkIterator implements ChunkIterator {
    // Persist the combined row-width budget; each half uses at most half of it.
    private final int width;
    private final Strip first, second;
    private final long pairedTotal, total;
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
        final int centerX = selection.centerChunkX();
        final int minZ = selection.centerChunkZ() - radiusZ;
        final long sizeX = 2L * radiusX + 1;
        final long sizeZ = 2L * radiusZ + 1;
        final int maxWidth = selection.pattern().getValue().map(Integer::parseInt)
                .orElseGet(GenerationSettings::maxWorkingCount);
        if (maxWidth < 1) {
            throw new IllegalArgumentException("Strip width must be positive");
        }
        this.width = (int) Math.min(2L * (radiusX + 1L), maxWidth);
        this.total = sizeX * sizeZ;
        final int share = Math.max(1, width / 2);
        final Strip left = new Strip(centerX - 1, minZ, -1, radiusX, sizeZ, share);
        final Strip right = new Strip(centerX, minZ, 1, (long) radiusX + 1, sizeZ, share);
        // Both streams start equally far from the center unless the area is only one row high.
        this.first = radiusZ == 0 ? right : left;
        this.second = radiusZ == 0 ? left : right;
        // A one-request budget visits one half at a time rather than alternating distant fronts.
        this.pairedTotal = width > 1 ? 2 * Math.min(first.total, second.total) : 0;
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
        final long index = current++;
        if (index < pairedTotal) {
            return (index % 2 == 0 ? first : second).at(index / 2);
        }
        if (width > 1) {
            final Strip remaining = first.total > second.total ? first : second;
            return remaining.at(index - pairedTotal / 2);
        }
        return index < first.total ? first.at(index) : second.at(index - first.total);
    }

    @Override
    public long total() {
        return total;
    }

    @Override
    public String name() {
        return Parameter.of(PatternType.STRIP, String.valueOf(width)).toString();
    }

    private static final class Strip {
        private final int originX, minZ, direction, width;
        private final long sizeX, sizeZ, total;

        private Strip(final int originX, final int minZ, final int direction, final long sizeX,
                      final long sizeZ, final int share) {
            this.originX = originX;
            this.minZ = minZ;
            this.direction = direction;
            this.sizeX = sizeX;
            this.sizeZ = sizeZ;
            this.width = (int) Math.min(sizeX, share);
            this.total = sizeX * sizeZ;
        }

        private ChunkCoordinate at(final long index) {
            final long stripSize = width * sizeZ;
            final long stripX = index / stripSize * width;
            final long offset = index % stripSize;
            final long rowWidth = Math.min(width, sizeX - stripX);
            return new ChunkCoordinate((int) (originX + direction * (stripX + offset % rowWidth)),
                    (int) (minZ + offset / rowWidth));
        }
    }
}
