package org.popcraft.chunky.iterator;

import org.junit.Test;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.util.ChunkCoordinate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * This test compares the boundaries of the generated region for each iterator to make sure they are consistent
 * with one another.
 */
public class BoundaryTest {
    private static final Selection SELECTION = Selection.builder(null, null).center(-25, 25).radius(50).build();

    @Test
    public void boundaries() {
        final ChunkIterator concentricIterator = new ConcentricChunkIterator(SELECTION);
        final ChunkIterator loop2Iterator = new Loop2ChunkIterator(SELECTION);
        final ChunkIterator spiralIterator = new SpiralChunkIterator(SELECTION);
        final List<ChunkCoordinate> concentricCoordinates = new ArrayList<>();
        final List<ChunkCoordinate> loop2Coordinates = new ArrayList<>();
        final List<ChunkCoordinate> spiralCoordinates = new ArrayList<>();
        concentricIterator.forEachRemaining(concentricCoordinates::add);
        loop2Iterator.forEachRemaining(loop2Coordinates::add);
        spiralIterator.forEachRemaining(spiralCoordinates::add);
        Collections.sort(concentricCoordinates);
        Collections.sort(loop2Coordinates);
        Collections.sort(spiralCoordinates);
        final ChunkCoordinate concentricPoint1 = concentricCoordinates.get(0);
        final ChunkCoordinate concentricPoint2 = concentricCoordinates.get(concentricCoordinates.size() - 1);
        final ChunkCoordinate loop2Point1 = loop2Coordinates.get(0);
        final ChunkCoordinate loop2Point2 = loop2Coordinates.get(loop2Coordinates.size() - 1);
        final ChunkCoordinate spiralPoint1 = spiralCoordinates.get(0);
        final ChunkCoordinate spiralPoint2 = spiralCoordinates.get(spiralCoordinates.size() - 1);
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
