package org.popcraft.chunky;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class Selection {
    public World world = Bukkit.getServer().getWorlds().get(0);
    public int x = 0;
    public int z = 0;
    public int radius = 500;
    public String pattern = "concentric";
    public String shape = "square";
    public boolean silent;
    public int quiet = 1;
}
