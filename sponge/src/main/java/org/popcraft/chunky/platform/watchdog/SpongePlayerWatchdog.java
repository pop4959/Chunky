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
        super(chunky.getChunky());
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
        return super.getConfiguredPlayerCount() >= playerCount.get();
    }

    @Override
    public void stop() {
        Sponge.getEventManager().unregisterListeners(this);
    }
}
