package org.popcraft.chunky.platform.watchdog;

import org.popcraft.chunky.ChunkySponge;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.concurrent.atomic.AtomicInteger;

public class SpongePlayerWatchdog extends PlayerWatchdog {

    private AtomicInteger playercount;
    private ChunkySponge chunky;

    public SpongePlayerWatchdog(ChunkySponge chunky) {
        this.chunky = chunky;
        Sponge.getEventManager().registerListeners(chunky, this);
    }

    @Listener
    private void onPlayerJoin(ClientConnectionEvent.Join event) {
        playercount.incrementAndGet();
    }

    @Listener
    private void onPlayerLeave(ClientConnectionEvent.Disconnect event) {
        playercount.decrementAndGet();
    }

    @Override
    public boolean allowsGenerationRun() {

        return false;
    }

    @Override
    public void stop() {
        Sponge.getEventManager().unregisterListeners(this);
    }
}
