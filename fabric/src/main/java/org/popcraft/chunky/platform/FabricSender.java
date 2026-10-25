package org.popcraft.chunky.platform;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.popcraft.chunky.platform.util.Location;

import static org.popcraft.chunky.util.Translator.translateKey;

public class FabricSender implements Sender {

    private final CommandSourceStack source;

    public FabricSender(final CommandSourceStack source) {
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
        return new FabricWorld(source.getLevel());
    }

    @Override
    public Location getLocation() {
        final Vec3 pos = source.getPosition();
        final Vec2 rot = source.getRotation();
        return new Location(getWorld(), pos.x(), pos.y(), pos.z(), rot.x, rot.y);
    }

    @Override
    public boolean hasPermission(final String permission) {
        int firstDot = permission.indexOf('.');
        return hasPermission(permission.substring(0, firstDot), permission.substring(firstDot + 1), false);
    }

    public boolean hasPermission(final String namespace, final String permission, final boolean defaultOp) {
        if (defaultOp) {
            return source.checkPermission(Identifier.fromNamespaceAndPath(namespace, permission), PermissionLevel.GAMEMASTERS);
        } else {
            return source.checkPermission(Identifier.fromNamespaceAndPath(namespace, permission), false);
        }
    }

    @Override
    public void sendMessage(final String key, final boolean prefixed, final Object... args) {
        source.sendSuccess(() -> Component.nullToEmpty(translateKey(key, prefixed, args).replaceAll("&[0-9a-fk-orA-FK-OR]", "")), false);
    }
}
