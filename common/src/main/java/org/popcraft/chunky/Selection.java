package org.popcraft.chunky;

import org.popcraft.chunky.platform.World;

public class Selection {
    public World world;
    public int centerX;
    public int centerZ;
    public int radiusX;
    public int radiusZ;
    public String pattern;
    public String shape;
    public boolean silent;
    public int quiet;

    public Selection(Chunky chunky) {
        this.world = chunky.getPlatform().getServer().getWorlds().get(0);
        this.radiusX = 500;
        this.radiusZ = radiusX;
        this.pattern = "concentric";
        this.shape = "square";
        this.quiet = 1;
    }

    public Selection(int x, int z, int radius) {
        this.centerX = x;
        this.centerZ = z;
        this.radiusX = radius;
        this.radiusZ = radius;
    }

    public int getChunkX() {
        return centerX >> 4;
    }

    public int getChunkZ() {
        return centerZ >> 4;
    }

    public int getRadiusChunksX() {
        return (int) Math.ceil(radiusX / 16f);
    }

    public int getRadiusChunksZ() {
        return (int) Math.ceil(radiusZ / 16f);
    }

    public int getDiameterChunksX() {
        return 2 * getRadiusChunksX() + 1;
    }

    public int getDiameterChunksZ() {
        return 2 * getRadiusChunksZ() + 1;
    }
}
