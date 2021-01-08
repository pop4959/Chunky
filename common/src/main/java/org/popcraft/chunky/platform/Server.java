package org.popcraft.chunky.platform;

import org.popcraft.chunky.integration.Integration;
import org.popcraft.chunky.platform.watchdog.Watchdogs;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Server {
    Map<String, Integration> getIntegrations();

    Optional<World> getWorld(String name);

    List<World> getWorlds();

    Sender getConsoleSender();

    Scheduler getScheduler();

    Config getConfig();

    Watchdogs getWatchdogs();
}
