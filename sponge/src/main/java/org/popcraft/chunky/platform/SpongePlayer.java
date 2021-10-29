package org.popcraft.chunky.platform;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.popcraft.chunky.platform.util.Location;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.util.UUID;

import static org.popcraft.chunky.util.Translator.translateKey;

public class SpongePlayer extends SpongeSender implements Player {
    private final ServerPlayer player;

    public SpongePlayer(ServerPlayer player) {
        super(player);
        this.player = player;
    }

    @Override
    public boolean isPlayer() {
        return true;
    }

    @Override
    public String getName() {
        return player.name();
    }

    @Override
    public World getWorld() {
        return new SpongeWorld(player.serverLocation().world());
    }

    @Override
    public Location getLocation() {
        final ServerLocation loc = player.serverLocation();
        final Vector3d rot = player.rotation();
        return new Location(getWorld(), loc.x(), loc.y(), loc.z(), rot.floorX(), rot.floorY());
    }

    @Override
    public UUID getUUID() {
        return player.uniqueId();
    }

    @Override
    public void teleport(Location location) {
        player.setLocation(ServerLocation.of(((SpongeWorld) location.getWorld()).getWorld(), location.getX(), location.getY(), location.getZ()));
        player.setRotation(Vector3d.from(location.getYaw(), location.getPitch(), 0));
    }

    @Override
    public void sendActionBar(String key) {
        player.sendActionBar(LegacyComponentSerializer.legacyAmpersand().deserialize(translateKey(key, false)));
    }
}
