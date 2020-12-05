package org.popcraft.chunky.iterator;

import org.junit.Test;
import org.popcraft.chunky.util.ChunkCoordinate;
import org.popcraft.chunky.Selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * This test compares the boundaries of the generated region for each iterator to make sure they are consistent
 * with one another.
 */
public class BoundaryTest {
    private static final Selection SELECTION = new Selection(-25, 25, 50);

    @Test
    public void boundaries() {
        ChunkIterator concentricIterator = new ConcentricChunkIterator(SELECTION);
        ChunkIterator loop2Iterator = new Loop2ChunkIterator(SELECTION);
        ChunkIterator spiralIterator = new SpiralChunkIterator(SELECTION);
        List<ChunkCoordinate> concentricCoordinates = new ArrayList<>();
        List<ChunkCoordinate> loop2Coordinates = new ArrayList<>();
        List<ChunkCoordinate> spiralCoordinates = new ArrayList<>();
        concentricIterator.forEachRemaining(concentricCoordinates::add);
        loop2Iterator.forEachRemaining(loop2Coordinates::add);
        spiralIterator.forEachRemaining(spiralCoordinates::add);
        Collections.sort(concentricCoordinates);
        Collections.sort(loop2Coordinates);
        Collections.sort(spiralCoordinates);
        ChunkCoordinate concentricPoint1 = concentricCoordinates.get(0);
        ChunkCoordinate concentricPoint2 = concentricCoordinates.get(concentricCoordinates.size() - 1);
        ChunkCoordinate loop2Point1 = loop2Coordinates.get(0);
        ChunkCoordinate loop2Point2 = loop2Coordinates.get(loop2Coordinates.size() - 1);
        ChunkCoordinate spiralPoint1 = spiralCoordinates.get(0);
        ChunkCoordinate spiralPoint2 = spiralCoordinates.get(spiralCoordinates.size() - 1);
        assertEquals(concentricPoint1.x, loop2Point1.x);
        assertEquals(concentricPoint1.z, loop2Point1.z);
        assertEquals(loop2Point1.x, spiralPoint1.x);
        assertEquals(loop2Point1.z, spiralPoint1.z);
        assertEquals(concentricPoint2.x, loop2Point2.x);
        assertEquals(concentricPoint2.z, loop2Point2.z);
        assertEquals(loop2Point2.x, spiralPoint2.x);
        assertEquals(loop2Point2.z, spiralPoint2.z);
    }
}
