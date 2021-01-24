package org.popcraft.chunky;

import org.popcraft.chunky.platform.World;

import java.util.Optional;

public class Selection {
    public World world;
    public int centerX = 0;
    public int centerZ = 0;
    public int radiusX = 500;
    public int radiusZ = 500;
    public String pattern = "concentric";
    public String shape = "square";
    public boolean silent;
    public int quiet = 1;

    public Selection() {
    }

    public Selection(Chunky chunky) {
        this.world = chunky.getPlatform().getServer().getWorlds().get(0);
    }

    public Selection(int x, int z, int radius) {
        this.centerX = x;
        this.centerZ = z;
        this.radiusX = radius;
        this.radiusZ = radius;
    }

    public Optional<World> getWorld() {
        return Optional.ofNullable(world);
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
