package org.popcraft.chunky.platform;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.handler.DefaultPermissionHandler;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;
import org.popcraft.chunky.platform.util.Location;

import java.util.HashMap;
import java.util.Map;

import static org.popcraft.chunky.util.Translator.translateKey;

public class ForgeSender implements Sender {
    private static final Map<String, PermissionNode<Boolean>> PERMISSION_NODES = new HashMap<>();
    private final CommandSourceStack source;

    static {
        addPermissionNode("chunky", "command", true);
        addPermissionNode("chunky", "progress.bossbar", true);
        addPermissionNode("chunkyborder", "bypass.move", false);
        addPermissionNode("chunkyborder", "bypass.place", false);
        addPermissionNode("chunkyborder", "bypass.break", false);
    }

    public ForgeSender(final CommandSourceStack source) {
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
        return new ForgeWorld(source.getLevel());
    }

    @Override
    public Location getLocation() {
        final Vec3 pos = source.getPosition();
        final Vec2 rot = source.getRotation();
        return new Location(getWorld(), pos.x(), pos.y(), pos.z(), rot.x, rot.y);
    }

    @Override
    public boolean hasPermission(final String permission) {
        if (source.getEntity() instanceof final ServerPlayer player) {
            final PermissionNode<Boolean> node = PERMISSION_NODES.get(permission);
            if (node != null && PermissionAPI.getRegisteredNodes().contains(node)) {
                return PermissionAPI.getPermission(player, node);
            }
            return !hasPermissionManager() && source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
        }
        return source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    public static void registerPermissions(final PermissionGatherEvent.Nodes event) {
        for (final PermissionNode<Boolean> node : PERMISSION_NODES.values()) {
            if (!event.getNodes().contains(node)) {
                event.addNodes(node);
            }
        }
    }

    private static void addPermissionNode(final String namespace, final String path, final boolean defaultOp) {
        final PermissionNode<Boolean> node = new PermissionNode<>(namespace, path, PermissionTypes.BOOLEAN, (player, uuid, context) -> player != null && player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) && (defaultOp || !hasPermissionManager()));
        PERMISSION_NODES.put(node.getNodeName(), node);
    }

    private static boolean hasPermissionManager() {
        final Identifier handler = PermissionAPI.getActivePermissionHandler();
        return handler != null && !DefaultPermissionHandler.IDENTIFIER.equals(handler);
    }

    @Override
    public void sendMessage(final String key, final boolean prefixed, final Object... args) {
        source.sendSuccess(() -> Component.nullToEmpty(translateKey(key, prefixed, args).replaceAll("&[0-9a-fk-orA-FK-OR]", "")), false);
    }
}
