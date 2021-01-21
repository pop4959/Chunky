package org.popcraft.chunky.platform.watchdog;

import org.popcraft.chunky.ChunkySponge;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.concurrent.atomic.AtomicInteger;

public class SpongePlayerWatchdog extends PlayerWatchdog {

    private AtomicInteger playerCount;
    private ChunkySponge chunky;

    public SpongePlayerWatchdog(ChunkySponge chunky) {
        this.playerCount = new AtomicInteger();
        this.chunky = chunky;
        Sponge.getEventManager().registerListeners(chunky, this);
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        playerCount.incrementAndGet();
    }

    @Listener
    public void onPlayerLeave(ClientConnectionEvent.Disconnect event) {
        playerCount.decrementAndGet();
    }

    @Override
    public boolean allowsGenerationRun() {
        return this.chunky.getChunky().getConfig().getWatchdogStartOn("players") >= playerCount.get();
    }

    @Override
    public void stop() {
        Sponge.getEventManager().unregisterListeners(this);
    }
}
