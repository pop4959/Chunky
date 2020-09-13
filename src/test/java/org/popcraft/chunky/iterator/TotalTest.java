package org.popcraft.chunky.iterator;

import org.junit.Test;
import org.popcraft.chunky.Selection;

import static org.junit.Assert.assertEquals;

/**
 * This test checks to make sure the total number of chunks generated matches across iterators.
 */
public class TotalTest {
    private static final Selection SELECTION = new Selection(-25, 25, 50);

    /**
     * Checks that the totals still match when the radius is changed.
     */
    @Test
    public void radius() {
        Selection s = new Selection(SELECTION.x, SELECTION.z, SELECTION.radius);
        for (int i = 0; i < SELECTION.radius; ++i) {
            s.radius = i;
            s.zRadius = i;
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
        Selection s = new Selection(SELECTION.x, SELECTION.z, SELECTION.radius);
        for (int i = 0; i > SELECTION.x; --i) {
            for (int j = 0; j < SELECTION.z; ++j) {
                s.x = i;
                s.z = j;
                ChunkIterator concentricIterator = new ConcentricChunkIterator(s);
                ChunkIterator loop2Iterator = new Loop2ChunkIterator(s);
                ChunkIterator spiralIterator = new SpiralChunkIterator(s);
                assertEquals(concentricIterator.total(), loop2Iterator.total());
                assertEquals(loop2Iterator.total(), spiralIterator.total());
            }
        }
    }
}
