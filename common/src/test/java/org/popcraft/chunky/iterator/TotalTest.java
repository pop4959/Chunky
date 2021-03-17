package org.popcraft.chunky.iterator;

import org.junit.Test;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.TestWorld;

import static org.junit.Assert.assertEquals;

/**
 * This test checks to make sure the total number of chunks generated matches across iterators.
 */
public class TotalTest {
    private static final Selection.Builder SELECTION = Selection.builder(new TestWorld()).center(-25, 25).radius(50);

    /**
     * Checks that the totals still match when the radius is changed.
     */
    @Test
    public void radius() {
        Selection original = SELECTION.build();
        for (int i = 0; i < original.radiusX(); ++i) {
            Selection s = SELECTION.radiusX(i).radiusZ(i).build();
            ChunkIterator concentricIterator = new ConcentricChunkIterator(s);
            ChunkIterator loop2Iterator = new Loop2ChunkIterator(s);
            ChunkIterator spiralIterator = new SpiralChunkIterator(s);
            assertEquals(concentricIterator.total(), loop2Iterator.total());
            assertEquals(loop2Iterator.total(), spiralIterator.total());
        }
    }

    /**
     * Checks that the totals still match when the center is moved.
     */
    @Test
    public void center() {
        Selection original = SELECTION.build();
        for (int i = 0; i > original.centerX(); --i) {
            for (int j = 0; j < original.centerZ(); ++j) {
                Selection s = SELECTION.center(i, j).build();
                ChunkIterator concentricIterator = new ConcentricChunkIterator(s);
                ChunkIterator loop2Iterator = new Loop2ChunkIterator(s);
                ChunkIterator spiralIterator = new SpiralChunkIterator(s);
                assertEquals(concentricIterator.total(), loop2Iterator.total());
                assertEquals(loop2Iterator.total(), spiralIterator.total());
            }
        }
    }
}
