package org.popcraft.chunky.util;

import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;

public class RegionCache {
    private final Map<String, WorldState> cache = new ConcurrentHashMap<>();

    public WorldState getWorld(final String world) {
        return cache.computeIfAbsent(world, x -> new WorldState());
    }

    public void clear(final String world) {
        cache.remove(world);
    }

    public static final class WorldState {
        private final Map<Long, StampedBitSet> regions = new ConcurrentHashMap<>();

        public void setGenerated(final int x, final int z) {
            final long regionKey = ChunkMath.pack(x >> 5, z >> 5);
            final StampedBitSet region = regions.computeIfAbsent(regionKey, v -> new StampedBitSet());
            region.set(ChunkMath.regionIndex(x, z));
        }

        public boolean isGenerated(final int x, final int z) {
            final long regionKey = ChunkMath.pack(x >> 5, z >> 5);
            final StampedBitSet region = regions.get(regionKey);
            if (region == null) {
                return false;
            }
            return region.get(ChunkMath.regionIndex(x, z));
        }

        /**
         * A BitSet guarded by a StampedLock for reduced contention.
         * Reads use optimistic locking (no lock acquisition in the common case).
         * Writes use an exclusive write lock.
         */
        private static final class StampedBitSet {
            private final BitSet bits = new BitSet(1024); // 32×32 chunks per region
            private final StampedLock lock = new StampedLock();

            void set(final int index) {
                final long stamp = lock.writeLock();
                try {
                    bits.set(index);
                } finally {
                    lock.unlockWrite(stamp);
                }
            }

            boolean get(final int index) {
                // Optimistic read — no lock acquisition if no concurrent write is happening.
                long stamp = lock.tryOptimisticRead();
                final boolean result = bits.get(index);
                if (!lock.validate(stamp)) {
                    // Fall back to a full read lock if a write occurred concurrently.
                    stamp = lock.readLock();
                    try {
                        return bits.get(index);
                    } finally {
                        lock.unlockRead(stamp);
                    }
                }
                return result;
            }
        }
    }
}
