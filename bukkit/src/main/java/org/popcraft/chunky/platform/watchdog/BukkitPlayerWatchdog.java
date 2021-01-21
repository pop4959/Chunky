package org.popcraft.chunky.platform.watchdog;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.ChunkyBukkit;

import java.util.concurrent.atomic.AtomicInteger;

public class BukkitPlayerWatchdog extends PlayerWatchdog implements Listener {
    //We have to keep track of player count ourselves as allowsGenerationRun may run asynchronously
    private AtomicInteger playerCount;
    private ChunkyBukkit chunky;

    public BukkitPlayerWatchdog(ChunkyBukkit chunky) {
        this.chunky = chunky;
        this.playerCount = new AtomicInteger();
    }

    @Override
    public boolean allowsGenerationRun() {
        return this.chunky.getChunky().getConfig().getWatchdogStartOn("players") >= playerCount.get();
    }

    @Override
    public void startInternal() {
        chunky.getServer().getPluginManager().registerEvents(this, chunky);
    }

    @Override
    public void stopInternal() {
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
