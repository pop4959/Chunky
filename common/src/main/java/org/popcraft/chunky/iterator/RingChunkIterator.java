package org.popcraft.chunky.iterator;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.util.ChunkCoordinate;
import org.popcraft.chunky.util.Input;

import java.util.NoSuchElementException;

public class RingChunkIterator implements ChunkIterator {
    private static final boolean SPLIT = Boolean.getBoolean("chunky.ringSplit");
    private static final boolean ALTERNATING = Boolean.getBoolean("chunky.ringAlternating");
    private static final int WIDTH = Math.max(1, Input.tryInteger(System.getProperty("chunky.ringWidth")).orElse(64));
    private static final int CENTER_RADIUS = (WIDTH - 1) / 2;
    private final int centerX;
    private final int centerZ;
    private final int radius;
    private final long totalChunks;
    private RingChunkProgress currentRingProgress;
    private int ring;
    private boolean hasNext = true;

    public RingChunkIterator(final Selection selection, final long count) {
        this(selection);
        if (count <= 0) {
            return;
        }
        final long skip = Math.min(count, totalChunks);
        final long completed = ((long) Math.sqrt(skip) - 1) / 2;
        this.ring = completed < CENTER_RADIUS ? 0 : (int) ((completed - CENTER_RADIUS) / WIDTH) + 1;
        final long skipEdge = CENTER_RADIUS + (ring - 1L) * WIDTH;
        final long skipChunks = ring == 0 ? 0 : (2 * skipEdge + 1) * (2 * skipEdge + 1);
        this.currentRingProgress = nextRingChunkProgress(skip - skipChunks);
        if (currentRingProgress == null || !currentRingProgress.hasNext()) {
            hasNext = false;
        }
    }

    public RingChunkIterator(final Selection selection) {
        this.centerX = selection.centerChunkX();
        this.centerZ = selection.centerChunkZ();
        this.radius = selection.radiusChunksX();
        final long diameterChunks = selection.diameterChunksX();
        this.totalChunks = diameterChunks * diameterChunks;
        this.currentRingProgress = nextRingChunkProgress(0);
    }

    private RingChunkProgress nextRingChunkProgress(final long count) {
        final int inner = ring == 0 ? -1 : CENTER_RADIUS + (ring - 1) * WIDTH;
        if (inner >= radius) {
            return null;
        }
        final int outer = Math.min(CENTER_RADIUS + ring * WIDTH, radius);
        return new RingChunkProgress(ring++, inner, outer, count);
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public ChunkCoordinate next() {
        if (!hasNext) {
            throw new NoSuchElementException();
        }
        final ChunkCoordinate chunkCoord = currentRingProgress.next();
        while (currentRingProgress != null && !currentRingProgress.hasNext()) {
            currentRingProgress = nextRingChunkProgress(0);
        }
        if (currentRingProgress == null) {
            hasNext = false;
        }
        return chunkCoord;
    }

    @Override
    public long total() {
        return totalChunks;
    }

    @Override
    public String name() {
        return PatternType.RING;
    }

    public final class RingChunkProgress implements ChunkIterator {
        private final boolean alternate;
        private final int inner;
        private final int outer;
        private final int length;
        private final long side;
        private final long firstSideChunks;
        private final long total;
        private final String name;
        private long current;
        private boolean hasNext;

        public RingChunkProgress(final int ring, final int inner, final int outer, final long count) {
            this.alternate = ALTERNATING && ring % 2 == 1;
            this.inner = inner;
            this.outer = outer;
            this.length = outer - inner;
            this.side = 2L * outer + 1;
            this.firstSideChunks = side * length;
            this.total = side * side - (inner < 0 ? 0 : (2L * inner + 1) * (2L * inner + 1));
            this.current = Math.max(0, Math.min(count, total));
            this.hasNext = current < total;
            this.name = "ring_chunk_progress_%d".formatted(ring);
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public ChunkCoordinate next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            final int dx;
            final int dz;
            if (inner < 0) {
                dx = (int) (current % side) - outer;
                dz = (int) (current / side) - outer;
            } else {
                final boolean mirrored = SPLIT ? current % 2 != 0 : current >= total / 2;
                final long step = SPLIT ? current / 2 : mirrored ? current - total / 2 : current;
                final int sign = mirrored ? -1 : 1;
                if (step < firstSideChunks) {
                    dx = sign * ((int) (step / length) - outer);
                    dz = sign * (-inner - 1 - (int) (step % length));
                } else {
                    final long turnStep = step - firstSideChunks;
                    dx = sign * (inner + 1 + (int) (turnStep % length));
                    dz = sign * ((int) (turnStep / length) - inner);
                }
            }
            ++current;
            if (current >= total) {
                hasNext = false;
            }
            return alternate ? new ChunkCoordinate(centerX + dz, centerZ + dx) : new ChunkCoordinate(centerX + dx, centerZ + dz);
        }

        @Override
        public long total() {
            return total;
        }

        @Override
        public String name() {
            return name;
        }
    }
}
