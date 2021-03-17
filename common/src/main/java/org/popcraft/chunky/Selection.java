package org.popcraft.chunky;

import org.popcraft.chunky.platform.Border;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.util.Coordinate;

public class Selection {
    private final World world;
    private final int centerX;
    private final int centerZ;
    private final int radiusX;
    private final int radiusZ;
    private final String pattern;
    private final String shape;
    private final int centerChunkX;
    private final int centerChunkZ;
    private final int radiusChunksX;
    private final int radiusChunksZ;
    private final int diameterChunksX;
    private final int diameterChunksZ;

    private Selection(World world, int centerX, int centerZ, int radiusX, int radiusZ, String pattern, String shape) {
        this.world = world;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.radiusX = radiusX;
        this.radiusZ = radiusZ;
        this.pattern = pattern;
        this.shape = shape;
        this.centerChunkX = centerX >> 4;
        this.centerChunkZ = centerZ >> 4;
        this.radiusChunksX = (int) Math.ceil(radiusX / 16f);
        this.radiusChunksZ = (int) Math.ceil(radiusZ / 16f);
        this.diameterChunksX = 2 * radiusChunksX + 1;
        this.diameterChunksZ = 2 * radiusChunksZ + 1;
    }

    public World world() {
        return this.world;
    }

    public int centerX() {
        return this.centerX;
    }

    public int centerZ() {
        return this.centerZ;
    }

    public int radiusX() {
        return this.radiusX;
    }

    public int radiusZ() {
        return this.radiusZ;
    }

    public String pattern() {
        return this.pattern;
    }

    public String shape() {
        return this.shape;
    }

    public int centerChunkX() {
        return this.centerChunkX;
    }

    public int centerChunkZ() {
        return this.centerChunkZ;
    }

    public int radiusChunksX() {
        return this.radiusChunksX;
    }

    public int radiusChunksZ() {
        return this.radiusChunksZ;
    }

    public int diameterChunksX() {
        return this.diameterChunksX;
    }

    public int diameterChunksZ() {
        return this.diameterChunksZ;
    }

    public static Builder builder(World world) {
        return new Builder(world);
    }

    public static final class Builder {
        private World world;
        private int centerX = 0;
        private int centerZ = 0;
        private int radiusX = 500;
        private int radiusZ = 500;
        private String pattern = "concentric";
        private String shape = "square";

        private Builder(World world) {
            this.world = world;
        }

        public Builder world(World world) {
            this.world = world;
            return this;
        }

        public Builder center(int centerX, int centerZ) {
            this.centerX = centerX;
            this.centerZ = centerZ;
            return this;
        }

        public Builder centerX(int centerX) {
            this.centerX = centerX;
            return this;
        }

        public Builder centerZ(int centerZ) {
            this.centerZ = centerZ;
            return this;
        }

        public Builder radius(int radius) {
            this.radiusX = radius;
            this.radiusZ = radius;
            return this;
        }

        public Builder radiusX(int radiusX) {
            this.radiusX = radiusX;
            return this;
        }

        public Builder radiusZ(int radiusZ) {
            this.radiusZ = radiusZ;
            return this;
        }

        public Builder pattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder shape(String shape) {
            this.shape = shape;
            return this;
        }

        public Builder spawn() {
            Coordinate spawn = world.getSpawnCoordinate();
            this.centerX = spawn.getX();
            this.centerZ = spawn.getZ();
            return this;
        }

        public Builder worldborder() {
            Border border = world.getWorldBorder();
            Coordinate center = border.getCenter();
            this.centerX = center.getX();
            this.centerZ = center.getZ();
            this.radiusX = border.getRadiusX();
            this.radiusZ = border.getRadiusZ();
            return this;
        }

        public Selection build() {
            return new Selection(world, centerX, centerZ, radiusX, radiusZ, pattern, shape);
        }
    }
}
