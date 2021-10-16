package org.popcraft.chunky.platform;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.popcraft.chunky.platform.util.Location;

import java.util.UUID;

import static org.popcraft.chunky.util.Translator.translateKey;

public class ForgePlayer extends ForgeSender implements Player {
    private final ServerPlayer player;

    public ForgePlayer(ServerPlayer player) {
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
        return new ForgeWorld(player.getLevel());
    }

    @Override
    public Location getLocation() {
        return new Location(getWorld(), player.getX(), player.getY(), player.getZ(), player.getXRot(), player.getYRot());
    }

    @Override
    public void sendMessage(String key, boolean prefixed, Object... args) {
        player.sendMessage(Component.nullToEmpty(translateKey(key, prefixed, args).replaceAll("&(?=[0-9a-fk-orA-FK-OR])", "§")), player.getUUID());
    }

    @Override
    public UUID getUUID() {
        return player.getUUID();
    }
}
