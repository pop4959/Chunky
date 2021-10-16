package org.popcraft.chunky.platform;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.popcraft.chunky.platform.util.Location;

import static org.popcraft.chunky.util.Translator.translateKey;

public class BukkitSender implements Sender {
    final CommandSender sender;

    public BukkitSender(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof Player;
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public World getWorld() {
        return new BukkitWorld(Bukkit.getWorlds().get(0));
    }

    @Override
    public Location getLocation() {
        return new Location(getWorld(), 0, 0, 0, 0, 0);
    }

    @Override
    public void sendMessage(String key, boolean prefixed, Object... args) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', translateKey(key, prefixed, args)));
    }
}
