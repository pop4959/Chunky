package org.popcraft.chunky.platform;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;
import org.popcraft.chunky.platform.util.Location;

import java.util.EnumSet;
import java.util.UUID;

import static org.popcraft.chunky.util.Translator.translateKey;

public class FabricPlayer extends FabricSender implements Player {
    private final ServerPlayer player;

    public FabricPlayer(final ServerPlayer player) {
        super(player.createCommandSourceStack());
        this.player = player;
    }

    @Override
    public boolean isPlayer() {
        return true;
    }

    @Override
    public String getName() {
        return player.getName().toString();
    }

    @Override
    public World getWorld() {
        return new FabricWorld(player.serverLevel());
    }

    @Override
    public Location getLocation() {
        return new Location(getWorld(), player.getX(), player.getY(), player.getZ(), player.getXRot(), player.getYRot());
    }

    @Override
    public void sendMessage(final String key, final boolean prefixed, final Object... args) {
        player.sendSystemMessage(formatColored(translateKey(key, prefixed, args)));
    }

    @Override
    public UUID getUUID() {
        return player.getUUID();
    }

    @Override
    public void teleport(final Location location) {
        player.teleportTo(((FabricWorld) location.getWorld()).getWorld(), location.getX(), location.getY(), location.getZ(), EnumSet.noneOf(Relative.class), location.getYaw(), location.getPitch(), true);
    }

    @Override
    public void sendActionBar(final String key) {
        player.displayClientMessage(formatColored(translateKey(key, false)), true);
    }

    private Component formatColored(final String message) {
        return Component.nullToEmpty(message.replaceAll("&(?=[0-9a-fk-orA-FK-OR])", "§"));
    }
}
