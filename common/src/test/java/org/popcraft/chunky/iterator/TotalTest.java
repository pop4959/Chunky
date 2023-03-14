package org.popcraft.chunky.iterator;

import org.junit.Test;
import org.popcraft.chunky.Selection;

import static org.junit.Assert.assertEquals;

/**
 * This test checks to make sure the total number of chunks generated matches across iterators.
 */
public class TotalTest {
    private static final Selection.Builder SELECTION = Selection.builder(null, null).center(-25, 25).radius(50);

    /**
     * Checks that the totals still match when the radius is changed.
     */
    @Test
    public void radius() {
        final Selection original = SELECTION.build();
        for (int i = 0; i < original.radiusX(); ++i) {
            final Selection s = SELECTION.radiusX(i).radiusZ(i).build();
            final ChunkIterator concentricIterator = new ConcentricChunkIterator(s);
            final ChunkIterator loop2Iterator = new Loop2ChunkIterator(s);
            final ChunkIterator spiralIterator = new SpiralChunkIterator(s);
            final ChunkIterator regionIterator = new RegionChunkIterator(s);
            assertEquals(concentricIterator.total(), loop2Iterator.total());
            assertEquals(loop2Iterator.total(), spiralIterator.total());
            assertEquals(spiralIterator.total(), regionIterator.total());
        }
    }

    /**
     * Checks that the totals still match when the center is moved.
     */
    @Test
    public void center() {
        final Selection original = SELECTION.build();
        for (int i = 0; i > original.centerX(); --i) {
            for (int j = 0; j < original.centerZ(); ++j) {
                final Selection s = SELECTION.center(i, j).build();
                final ChunkIterator concentricIterator = new ConcentricChunkIterator(s);
                final ChunkIterator loop2Iterator = new Loop2ChunkIterator(s);
                final ChunkIterator spiralIterator = new SpiralChunkIterator(s);
                final ChunkIterator regionIterator = new RegionChunkIterator(s);
                assertEquals(concentricIterator.total(), loop2Iterator.total());
                assertEquals(loop2Iterator.total(), spiralIterator.total());
                assertEquals(spiralIterator.total(), regionIterator.total());
            }
        }
    }
}
