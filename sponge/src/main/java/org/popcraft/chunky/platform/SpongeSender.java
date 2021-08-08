package org.popcraft.chunky.platform;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.popcraft.chunky.util.Coordinate;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;

import static org.popcraft.chunky.util.Translator.translateKey;

public class SpongeSender implements Sender {
    private final Audience audience;

    public SpongeSender(Audience audience) {
        this.audience = audience;
    }

    @Override
    public boolean isPlayer() {
        return getPlayer().isPresent();
    }

    @Override
    public String getName() {
        return getPlayer().map(Player::name).orElse("Console");
    }

    @Override
    public Coordinate getCoordinate() {
        return getPlayer()
                .map(Player::location)
                .map(loc -> new Coordinate(loc.blockX(), loc.blockZ()))
                .orElse(new Coordinate(0, 0));
    }

    private Optional<Player> getPlayer() {
        if (audience instanceof Player) {
            return Optional.of((Player) audience);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void sendMessage(String key, boolean prefixed, Object... args) {
        audience.sendMessage(Identity.nil(), LegacyComponentSerializer.legacyAmpersand().deserialize(translateKey(key, prefixed, args)));
    }
}
