package org.popcraft.chunky.platform;

import org.popcraft.chunky.platform.util.Location;

import java.util.UUID;

public class BukkitPlayer extends BukkitSender implements Player {
    final org.bukkit.entity.Player player;

    public BukkitPlayer(org.bukkit.entity.Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public boolean isPlayer() {
        return true;
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public World getWorld() {
        return new BukkitWorld(player.getWorld());
    }

    @Override
    public Location getLocation() {
        org.bukkit.Location location = player.getLocation();
        return new Location(getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getPitch(), location.getYaw());
    }

    @Override
    public UUID getUUID() {
        return player.getUniqueId();
    }
}
