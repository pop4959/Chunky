package org.popcraft.chunky.platform;

import io.papermc.lib.PaperLib;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.popcraft.chunky.platform.util.Location;

import java.util.UUID;

import static org.popcraft.chunky.util.Translator.translateKey;

public class BukkitPlayer extends BukkitSender implements Player {
    private static final boolean ACTION_BAR_SUPPORTED;

    static {
        boolean barSupported;
        try {
            org.bukkit.entity.Player.class.getMethod("spigot");
            barSupported = true;
        } catch (NoSuchMethodException e) {
            barSupported = false;
        }
        ACTION_BAR_SUPPORTED = barSupported;
    }

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
        return new Location(getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    @Override
    public UUID getUUID() {
        return player.getUniqueId();
    }

    @Override
    public void teleport(Location location) {
        final org.bukkit.World world = Bukkit.getWorld(location.getWorld().getName());
        final org.bukkit.Location loc = new org.bukkit.Location(world, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        final Entity vehicle = player.getVehicle();
        PaperLib.teleportAsync(player, loc);
        if (vehicle != null) {
            PaperLib.teleportAsync(vehicle, loc);
        }
    }

    @Override
    public void sendActionBar(String key) {
        if (ACTION_BAR_SUPPORTED) {
            final String message = formatColored(translateKey(key, false));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        } else {
            this.sendMessage(key);
        }
    }
}
