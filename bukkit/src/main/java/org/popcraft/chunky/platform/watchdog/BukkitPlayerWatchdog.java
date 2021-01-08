package org.popcraft.chunky.platform.watchdog;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.popcraft.chunky.ChunkyBukkit;

public class BukkitPlayerWatchdog extends PlayerWatchdog implements Listener {
    //We have to keep track of player count ourselves as allowsGenerationRun may run asynchronously
    private int playerCount;
    private ChunkyBukkit chunky;

    public BukkitPlayerWatchdog(ChunkyBukkit chunky) {
        this.chunky = chunky;
        chunky.getServer().getPluginManager().registerEvents(this, chunky);
    }

    @Override
    public boolean allowsGenerationRun() {
        return this.chunky.getConfig().getInt("watchdogs.players.start-on", -1) >= playerCount;
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
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
