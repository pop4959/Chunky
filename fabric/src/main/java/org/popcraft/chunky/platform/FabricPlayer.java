package org.popcraft.chunky.platform;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.popcraft.chunky.platform.util.Location;

import java.util.UUID;

import static org.popcraft.chunky.util.Translator.translateKey;

public class FabricPlayer extends FabricSender implements Player {
    private final ServerPlayerEntity player;

    public FabricPlayer(ServerPlayerEntity player) {
        super(player.getCommandSource());
        this.player = player;
    }

    @Override
    public boolean isPlayer() {
        return true;
    }

    @Override
    public String getName() {
        return player.getName().getString();
    }

    @Override
    public World getWorld() {
        return new FabricWorld(player.getServerWorld());
    }

    @Override
    public Location getLocation() {
        return new Location(getWorld(), player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
    }

    @Override
    public void sendMessage(String key, boolean prefixed, Object... args) {
        player.sendMessage(formatColored(translateKey(key, prefixed, args)), false);
    }

    @Override
    public UUID getUUID() {
        return player.getUuid();
    }

    @Override
    public void teleport(Location location) {
        player.teleport(((FabricWorld) location.getWorld()).getServerWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    @Override
    public void sendActionBar(String key) {
        player.sendMessage(formatColored(translateKey(key, false)), true);
    }

    private Text formatColored(String message) {
        return Text.of(message.replaceAll("&(?=[0-9a-fk-orA-FK-OR])", "§"));
    }
}
