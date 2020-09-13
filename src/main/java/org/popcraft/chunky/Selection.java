package org.popcraft.chunky;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class Selection {
    public World world;
    public int x;
    public int z;
    public int radius;
    public int zRadius;
    public String pattern;
    public String shape;
    public boolean silent;
    public int quiet;

    public Selection() {
        this.world = Bukkit.getServer().getWorlds().get(0);
        this.radius = 500;
        this.zRadius = radius;
        this.pattern = "concentric";
        this.shape = "square";
        this.quiet = 1;
    }

    public Selection(int x, int z, int radius) {
        this.x = x;
        this.z = z;
        this.radius = radius;
        this.zRadius = radius;
    }

    public int getChunkX() {
        return x >> 4;
    }

    public int getChunkZ() {
        return z >> 4;
    }

    public int getRadiusChunks() {
        return (int) Math.ceil(radius / 16f);
    }

    public int getRadiusChunksZ() {
        return (int) Math.ceil(zRadius / 16f);
    }

    public int getDiameterChunks() {
        return 2 * getRadiusChunks() + 1;
    }

    public int getDiameterChunksZ() {
        return 2 * getRadiusChunksZ() + 1;
    }
}
