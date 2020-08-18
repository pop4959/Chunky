package org.popcraft.chunky.iterator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * This test checks to make sure the total number of chunks generated matches across iterators.
 */
public class TotalTest {
    private static final int RADIUS = 50, X_CENTER = -25, Z_CENTER = 25;

    /**
     * Checks that the totals still match when the radius is changed.
     */
    @Test
    public void radius() {
        for (int i = 0; i < RADIUS; ++i) {
            ChunkIterator concentricIterator = new ConcentricChunkIterator(i, X_CENTER, Z_CENTER);
            ChunkIterator loop2Iterator = new Loop2ChunkIterator(i, X_CENTER, Z_CENTER);
            ChunkIterator spiralIterator = new SpiralChunkIterator(i, X_CENTER, Z_CENTER);
            assertEquals(concentricIterator.total(), loop2Iterator.total());
            assertEquals(loop2Iterator.total(), spiralIterator.total());
        }
    }

    /**
     * Checks that the totals still match when the center is moved.
     */
    @Test
    public void center() {
        for (int i = 0; i > X_CENTER; --i) {
            for (int j = 0; j < Z_CENTER; ++j) {
                ChunkIterator concentricIterator = new ConcentricChunkIterator(RADIUS, i, j);
                ChunkIterator loop2Iterator = new Loop2ChunkIterator(RADIUS, i, j);
                ChunkIterator spiralIterator = new SpiralChunkIterator(RADIUS, i, j);
                assertEquals(concentricIterator.total(), loop2Iterator.total());
                assertEquals(loop2Iterator.total(), spiralIterator.total());
            }
        }
    }
}
