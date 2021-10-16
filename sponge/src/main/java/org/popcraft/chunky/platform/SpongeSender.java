package org.popcraft.chunky.platform;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.popcraft.chunky.platform.util.Location;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import static org.popcraft.chunky.util.Translator.translateKey;

public class SpongeSender implements Sender {
    private final Audience audience;

    public SpongeSender(Audience audience) {
        this.audience = audience;
    }

    @Override
    public boolean isPlayer() {
        return audience instanceof Player;
    }

    @Override
    public String getName() {
        return "Console";
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
    public void sendMessage(String key, boolean prefixed, Object... args) {
        audience.sendMessage(Identity.nil(), LegacyComponentSerializer.legacyAmpersand().deserialize(translateKey(key, prefixed, args)));
    }
}
