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
        return new Location(getWorld(), player.getX(), player.getY(), player.getZ(), player.getPitch(), player.getYaw());
    }

    @Override
    public void sendMessage(String key, boolean prefixed, Object... args) {
        player.sendMessage(Text.of(translateKey(key, prefixed, args).replaceAll("&(?=[0-9a-fk-orA-FK-OR])", "§")), false);
    }

    @Override
    public UUID getUUID() {
        return player.getUuid();
    }
}
