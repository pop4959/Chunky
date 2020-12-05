package org.popcraft.chunky.platform;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.popcraft.chunky.Chunky.translate;

public class BukkitSender implements Sender {
    CommandSender sender;

    public BukkitSender(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof Player;
    }

    @Override
    public void sendMessage(String key, Object... args) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', translate(key, args)));
    }
}
