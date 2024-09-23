package org.popcraft.chunky.platform;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.popcraft.chunky.platform.util.Location;

import static org.popcraft.chunky.util.Translator.translateKey;

public class FabricSender implements Sender {
    private static final boolean HAS_PERMISSIONS;

    static {
        boolean hasPermissions;
        try {
            Class.forName("me.lucko.fabric.api.permissions.v0.Permissions");
            hasPermissions = true;
        } catch (ClassNotFoundException e) {
            hasPermissions = false;
        }
        HAS_PERMISSIONS = hasPermissions;
    }

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
        return hasPermission(permission, false);
    }

    public boolean hasPermission(final String permission, final boolean defaultOp) {
        if (HAS_PERMISSIONS) {
            if (defaultOp) {
                return Permissions.check(source, permission, 2);
            } else {
                return Permissions.check(source, permission, false);
            }
        } else {
            return source.hasPermission(2);
        }
    }

    @Override
    public void sendMessage(final String key, final boolean prefixed, final Object... args) {
        source.sendSuccess(() -> Component.nullToEmpty(translateKey(key, prefixed, args).replaceAll("&[0-9a-fk-orA-FK-OR]", "")), false);
    }
}
