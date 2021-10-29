package org.popcraft.chunky.platform;

import org.popcraft.chunky.platform.util.Location;

import java.util.UUID;

public interface Player extends Sender {
    UUID getUUID();

    void teleport(Location location);

    void sendActionBar(String key);
}
