package org.popcraft.chunky.platform;

import org.popcraft.chunky.platform.util.Location;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.util.UUID;

public class SpongePlayer extends SpongeSender implements Player {
    private final org.spongepowered.api.entity.living.player.Player player;

    public SpongePlayer(org.spongepowered.api.entity.living.player.Player player) {
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
}
