package org.popcraft.chunky.iterator;

import org.junit.Test;
import org.popcraft.chunky.util.ChunkCoordinate;
import org.popcraft.chunky.Selection;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This test compares the default and continuation constructors of chunk iterator to ensure that the results they
 * return are consistent with each other.
 */
public class ConstructorTest {
    private static final Selection SELECTION = new Selection(-25, 25, 50);

    @Test
    public void concentric() {
        List<ChunkCoordinate> chunks = new ArrayList<>();
        ChunkIterator chunkIterator = new ConcentricChunkIterator(SELECTION);
        chunkIterator.forEachRemaining(chunks::add);
        int total = (int) chunkIterator.total();
        for (int i = 0; i < total; ++i) {
            List<ChunkCoordinate> continueChunks = new ArrayList<>();
            ChunkIterator continueIterator = new ConcentricChunkIterator(SELECTION, i);
            continueIterator.forEachRemaining(continueChunks::add);
            int continueTotal = (int) continueIterator.total();
            assertEquals("Total", total, continueTotal);
            int size = chunks.size(), continueSize = continueChunks.size();
            assertEquals("Continued Size", size - i, continueSize);
            for (int j = 0; j < continueSize; ++j) {
                int chunkX = chunks.get(j + i).x, chunkZ = chunks.get(j + i).z;
                int continueChunkX = continueChunks.get(j).x, continueChunkZ = continueChunks.get(j).z;
                assertTrue(chunkX == continueChunkX && chunkZ == continueChunkZ);
            }
        }
    }

    @Test
    public void loop2() {
        List<ChunkCoordinate> chunks = new ArrayList<>();
        ChunkIterator chunkIterator = new Loop2ChunkIterator(SELECTION);
        chunkIterator.forEachRemaining(chunks::add);
        int total = (int) chunkIterator.total();
        for (int i = 0; i < total; ++i) {
            List<ChunkCoordinate> continueChunks = new ArrayList<>();
            ChunkIterator continueIterator = new Loop2ChunkIterator(SELECTION, i);
            continueIterator.forEachRemaining(continueChunks::add);
            int continueTotal = (int) continueIterator.total();
            assertEquals("Total", total, continueTotal);
            int size = chunks.size(), continueSize = continueChunks.size();
            assertEquals("Continued Size", size - i, continueSize);
            for (int j = 0; j < continueSize; ++j) {
                int chunkX = chunks.get(j + i).x, chunkZ = chunks.get(j + i).z;
                int continueChunkX = continueChunks.get(j).x, continueChunkZ = continueChunks.get(j).z;
                assertTrue(chunkX == continueChunkX && chunkZ == continueChunkZ);
            }
        }
    }

    @Test
    public void spiral() {
        List<ChunkCoordinate> chunks = new ArrayList<>();
        ChunkIterator chunkIterator = new SpiralChunkIterator(SELECTION);
        chunkIterator.forEachRemaining(chunks::add);
        int total = (int) chunkIterator.total();
        for (int i = 0; i < total; ++i) {
            List<ChunkCoordinate> continueChunks = new ArrayList<>();
            ChunkIterator continueIterator = new SpiralChunkIterator(SELECTION, i);
            continueIterator.forEachRemaining(continueChunks::add);
            int continueTotal = (int) continueIterator.total();
            assertEquals("Total", total, continueTotal);
            int size = chunks.size(), continueSize = continueChunks.size();
            assertEquals("Continued Size", size - i, continueSize);
            for (int j = 0; j < continueSize; ++j) {
                int chunkX = chunks.get(j + i).x, chunkZ = chunks.get(j + i).z;
                int continueChunkX = continueChunks.get(j).x, continueChunkZ = continueChunks.get(j).z;
                assertTrue(chunkX == continueChunkX && chunkZ == continueChunkZ);
            }
        }
    }

    @Test
    public void rectangle() {
        List<ChunkCoordinate> chunks = new ArrayList<>();
        Selection s = new Selection(-25, 25, 50);
        s.radiusZ = 100;
        ChunkIterator chunkIterator = new Loop2ChunkIterator(s);
        chunkIterator.forEachRemaining(chunks::add);
        int total = (int) chunkIterator.total();
        for (int i = 0; i < total; ++i) {
            List<ChunkCoordinate> continueChunks = new ArrayList<>();
            ChunkIterator continueIterator = new Loop2ChunkIterator(s, i);
            continueIterator.forEachRemaining(continueChunks::add);
            int continueTotal = (int) continueIterator.total();
            assertEquals("Total", total, continueTotal);
            int size = chunks.size(), continueSize = continueChunks.size();
            assertEquals("Continued Size", size - i, continueSize);
            for (int j = 0; j < continueSize; ++j) {
                int chunkX = chunks.get(j + i).x, chunkZ = chunks.get(j + i).z;
                int continueChunkX = continueChunks.get(j).x, continueChunkZ = continueChunks.get(j).z;
                assertTrue(chunkX == continueChunkX && chunkZ == continueChunkZ);
            }
        }
    }
}
