package org.popcraft.chunky.platform.watchdog;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.popcraft.chunky.ChunkyBukkit;
import org.popcraft.chunky.watchdog.AbstractGenerationWatchdog;

public class BukkitPlayerWatchdog extends AbstractGenerationWatchdog implements Listener {
    private int playerCount;
    private ChunkyBukkit chunky;

    public BukkitPlayerWatchdog(ChunkyBukkit chunky) {
        this.chunky = chunky;
        chunky.getServer().getPluginManager().registerEvents(this, chunky);
    }

    @Override
    public boolean allowsGenerationRun() {
        return this.chunky.getConfig().getInt("watchdogs.players.start-on") == playerCount;
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public String getStopReasonKey() {
        return "stop_player_online";
    }

    @Override
    public String getStartReasonKey() {
        return "start_no_players";
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        playerCount += 1;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        playerCount -= 1;
    }
}
