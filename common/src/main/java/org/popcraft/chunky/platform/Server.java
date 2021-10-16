package org.popcraft.chunky.platform;

import org.popcraft.chunky.integration.Integration;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Server {
    Map<String, Integration> getIntegrations();

    Optional<World> getWorld(String name);

    List<World> getWorlds();

    Sender getConsole();

    Collection<Player> getPlayers();

    Optional<Player> getPlayer(String name);

    Config getConfig();
}
