package org.popcraft.chunky.platform;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.popcraft.chunky.platform.util.Location;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.popcraft.chunky.util.Translator.translateKey;

public class FabricSender implements Sender {
    private static final List<String> PERMISSION_MANAGERS = List.of("luckperms", "player_roles");
    private static final boolean HAS_PERMISSION_MANAGER = PERMISSION_MANAGERS.stream().anyMatch(FabricLoader.getInstance()::isModLoaded);
    private static final Map<String, Optional<Identifier>> PERMISSION_IDS = new ConcurrentHashMap<>();

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
        return hasPermission(permission, !HAS_PERMISSION_MANAGER);
    }

    public boolean hasPermission(final String permission, final boolean defaultOp) {
        return toIdentifier(permission)
                .map(id -> defaultOp ? source.checkPermission(id, PermissionLevel.GAMEMASTERS) : source.checkPermission(id, false))
                .orElseGet(() -> defaultOp && source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));
    }

    private static Optional<Identifier> toIdentifier(final String permission) {
        return PERMISSION_IDS.computeIfAbsent(permission, key -> {
            final int separator = key.indexOf('.');
            if (separator <= 0 || separator == key.length() - 1) {
                return Optional.empty();
            }
            final String namespace = key.substring(0, separator).toLowerCase(Locale.ROOT);
            final String path = key.substring(separator + 1).toLowerCase(Locale.ROOT);
            return Optional.ofNullable(Identifier.tryBuild(namespace, path));
        });
    }

    @Override
    public void sendMessage(final String key, final boolean prefixed, final Object... args) {
        source.sendSuccess(() -> Component.nullToEmpty(translateKey(key, prefixed, args).replaceAll("&[0-9a-fk-orA-FK-OR]", "")), false);
    }
}
