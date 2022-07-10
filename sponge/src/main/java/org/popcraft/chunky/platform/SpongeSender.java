package org.popcraft.chunky.platform;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.popcraft.chunky.platform.util.Location;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.DefaultWorldKeys;
import org.spongepowered.api.world.server.WorldManager;

import static org.popcraft.chunky.util.Translator.translateKey;

public class SpongeSender implements Sender {
    private final Object source;

    public SpongeSender(final Object source) {
        this.source = source;
    }

    @Override
    public boolean isPlayer() {
        return source instanceof Player;
    }

    @Override
    public String getName() {
        return source instanceof final User user ? user.name() : "Console";
    }

    @Override
    public World getWorld() {
        final WorldManager worldManager = Sponge.game().server().worldManager();
        return new SpongeWorld(worldManager.world(DefaultWorldKeys.DEFAULT).orElseThrow(IllegalStateException::new));
    }

    @Override
    public Location getLocation() {
        return new Location(getWorld(), 0, 0, 0, 0, 0);
    }

    @Override
    public boolean hasPermission(final String permission) {
        return source instanceof final Subject subject && subject.hasPermission(permission);
    }

    @Override
    public void sendMessage(final String key, final boolean prefixed, final Object... args) {
        if (source instanceof final Audience audience) {
            audience.sendMessage(Identity.nil(), LegacyComponentSerializer.legacyAmpersand().deserialize(translateKey(key, prefixed, args)));
        }
    }
}
