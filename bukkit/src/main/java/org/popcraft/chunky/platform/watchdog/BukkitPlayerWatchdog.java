package org.popcraft.chunky.platform.watchdog;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.popcraft.chunky.ChunkyBukkit;

import java.util.concurrent.atomic.AtomicInteger;

public class BukkitPlayerWatchdog extends PlayerWatchdog implements Listener {
    //We have to keep track of player count ourselves as allowsGenerationRun may run asynchronously
    private AtomicInteger playerCount;

    public BukkitPlayerWatchdog(ChunkyBukkit chunky) {
        super(chunky.getChunky());
        this.playerCount = new AtomicInteger();
        chunky.getServer().getPluginManager().registerEvents(this, chunky);
    }

    @Override
    public boolean allowsGenerationRun() {
        return super.configuredPlayerCount >= playerCount.get();
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        playerCount.incrementAndGet();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        playerCount.decrementAndGet();
    }
}
