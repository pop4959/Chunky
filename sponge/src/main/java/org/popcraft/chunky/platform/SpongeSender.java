package org.popcraft.chunky.platform;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.popcraft.chunky.platform.util.Location;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.Subject;

import static org.popcraft.chunky.util.Translator.translateKey;

public class SpongeSender implements Sender {
    private final Object source;

    public SpongeSender(Object source) {
        this.source = source;
    }

    @Override
    public boolean isPlayer() {
        return source instanceof Player;
    }

    @Override
    public String getName() {
        return source instanceof User ? ((User) source).name() : "Console";
    }

    @Override
    public World getWorld() {
        return new SpongeWorld(Sponge.game().server().worldManager().defaultWorld());
    }

    @Override
    public Location getLocation() {
        return new Location(getWorld(), 0, 0, 0, 0, 0);
    }

    @Override
    public boolean hasPermission(String permission) {
        return source instanceof Subject && ((Subject) source).hasPermission(permission);
    }

    @Override
    public void sendMessage(String key, boolean prefixed, Object... args) {
        if (source instanceof Audience) {
            ((Audience) source).sendMessage(Identity.nil(), LegacyComponentSerializer.legacyAmpersand().deserialize(translateKey(key, prefixed, args)));
        }
    }
}
