package org.popcraft.chunky.platform;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.popcraft.chunky.platform.util.Location;
import org.popcraft.chunky.util.Translator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.popcraft.chunky.util.Translator.translateKey;

public class PaperSender implements Sender {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder().character('&').hexColors().build();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Map<String, Boolean> USES_MINI_MESSAGE = new ConcurrentHashMap<>();

    private final CommandSender sender;

    public PaperSender(final CommandSender sender) {
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
        return new PaperWorld(Bukkit.getWorlds().get(0));
    }

    @Override
    public Location getLocation() {
        return new Location(getWorld(), 0, 0, 0, 0, 0);
    }

    @Override
    public boolean hasPermission(final String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public void sendMessage(final String key, final boolean prefixed, final Object... args) {
        sender.sendMessage(format(key, prefixed, args));
    }

    protected static Component format(final String key, final boolean prefixed, final Object... args) {
        final Component message = LEGACY_SERIALIZER.deserialize(translateKey(key, prefixed, args));
        final boolean isMiniMessage = USES_MINI_MESSAGE.computeIfAbsent(Translator.getMessage(key), template -> !MINI_MESSAGE.stripTags(template).equals(template));
        if (!isMiniMessage) {
            return message;
        }
        return MINI_MESSAGE.deserialize(MINI_MESSAGE.serialize(message).replace("\\<", "<"));
    }
}
