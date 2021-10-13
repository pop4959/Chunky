package org.popcraft.chunky.platform;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.popcraft.chunky.util.Coordinate;

import java.util.Optional;
import java.util.UUID;

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
    public Optional<UUID> getUUID() {
        if (sender instanceof Player) {
            return Optional.of(((Player) sender).getUniqueId());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Coordinate getCoordinate() {
        if (sender instanceof Player) {
            Location location = ((Player) sender).getLocation();
            return new Coordinate(location.getBlockX(), location.getBlockZ());
        } else {
            return new Coordinate(0, 0);
        }
    }

    @Override
    public void sendMessage(String key, boolean prefixed, Object... args) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', translateKey(key, prefixed, args)));
    }
}
