package org.popcraft.chunky.util;

public class ChunkCoordinate implements Comparable<ChunkCoordinate> {
    public final int x, z;

    public ChunkCoordinate(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public int compareTo(ChunkCoordinate o) {
        return this.x == o.x ? Integer.compare(this.z, o.z) : Integer.compare(this.x, o.x);
    }

    @Override
    public String toString() {
        return String.format("%d, %d", x, z);
    }
}
