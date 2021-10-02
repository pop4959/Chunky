package org.popcraft.chunky.util;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkCoordinate that = (ChunkCoordinate) o;
        return x == that.x && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    public String toString() {
        return String.format("%d, %d", x, z);
    }
}
