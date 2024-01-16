package org.popcraft.chunky.platform;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.popcraft.chunky.platform.util.Location;

import static org.popcraft.chunky.util.Translator.translateKey;

public class NeoForgeSender implements Sender {
    private final CommandSourceStack source;

    public NeoForgeSender(final CommandSourceStack source) {
        this.source = source;
    }

    @Override
    public boolean isPlayer() {
        return source.getEntity() instanceof ServerPlayer;
    }

    @Override
    public String getName() {
        return source.getTextName();
    }

    @Override
    public World getWorld() {
        return new NeoForgeWorld(source.getLevel());
    }

    @Override
    public Location getLocation() {
        final Vec3 pos = source.getPosition();
        final Vec2 rot = source.getRotation();
        return new Location(getWorld(), pos.x(), pos.y(), pos.z(), rot.x, rot.y);
    }

    @Override
    public boolean hasPermission(final String permission) {
        return source.hasPermission(2);
    }

    @Override
    public void sendMessage(final String key, final boolean prefixed, final Object... args) {
        source.sendSuccess(() -> Component.nullToEmpty(translateKey(key, prefixed, args).replaceAll("&[0-9a-fk-orA-FK-OR]", "")), false);
    }
}
