package org.popcraft.chunky.platform;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec2f;
import org.popcraft.chunky.platform.util.Location;

import static org.popcraft.chunky.util.Translator.translateKey;

public class FabricSender implements Sender {
    private final ServerCommandSource source;

    public FabricSender(ServerCommandSource source) {
        this.source = source;
    }

    @Override
    public boolean isPlayer() {
        return source.getEntity() instanceof ServerPlayerEntity;
    }

    @Override
    public String getName() {
        return source.getName();
    }

    @Override
    public World getWorld() {
        return new FabricWorld(source.getWorld());
    }

    @Override
    public Location getLocation() {
        final Position pos = source.getPosition();
        final Vec2f rotation = source.getRotation();
        return new Location(getWorld(), pos.getX(), pos.getY(), pos.getZ(), rotation.x, rotation.y);
    }

    @Override
    public boolean hasPermission(String permission) {
        return source.hasPermissionLevel(2);
    }

    @Override
    public void sendMessage(String key, boolean prefixed, Object... args) {
        source.sendFeedback(Text.of(translateKey(key, prefixed, args).replaceAll("&[0-9a-fk-orA-FK-OR]", "")), false);
    }
}
