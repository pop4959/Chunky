package org.popcraft.chunky.platform;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.popcraft.chunky.platform.util.Location;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.popcraft.chunky.util.Translator.translateKey;

public class BukkitSender implements Sender {
    private static final Pattern RGB_PATTERN = Pattern.compile("&#[0-9a-fA-F]{6}");
    private static final boolean RGB_COLORS_SUPPORTED;

    static {
        boolean rgbSupported;
        try {
            ChatColor.class.getMethod("of", String.class);
            rgbSupported = true;
        } catch (NoSuchMethodException e) {
            rgbSupported = false;
        }
        RGB_COLORS_SUPPORTED = rgbSupported;
    }

    private final CommandSender sender;

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
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public void sendMessage(String key, boolean prefixed, Object... args) {
        sender.sendMessage(formatColored(translateKey(key, prefixed, args)));
    }

    protected String formatColored(String message) {
        if (RGB_COLORS_SUPPORTED) {
            Matcher rgbMatcher = RGB_PATTERN.matcher(message);
            while (rgbMatcher.find()) {
                final ChatColor rgbColor = ChatColor.of(rgbMatcher.group().substring(1));
                final String messageStart = message.substring(0, rgbMatcher.start());
                final String messageEnd = message.substring(rgbMatcher.end());
                message = messageStart + rgbColor + messageEnd;
                rgbMatcher = RGB_PATTERN.matcher(message);
            }
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
