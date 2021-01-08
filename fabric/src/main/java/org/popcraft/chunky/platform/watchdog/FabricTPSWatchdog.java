package org.popcraft.chunky.platform.watchdog;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.ChunkyFabric;
import org.popcraft.chunky.platform.FabricConfig;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

public class FabricTPSWatchdog extends TPSWatchdog {

    private int TICK_COUNT;
    private long[] TICK_TIMES = new long[100];
    private long lastTick = -1;
    private ChunkyFabric chunky;

    public FabricTPSWatchdog(ChunkyFabric chunky) {
        this.chunky = chunky;
        ServerTickEvents.START_SERVER_TICK.register(this::saveTickTime);
    }

    @Override
    public boolean allowsGenerationRun() {
        Optional<FabricConfig.ConfigModel> configModel = ((FabricConfig) chunky.getChunky().getConfig()).getConfigModel();
        if(configModel.isPresent()) {
            Map<String, FabricConfig.WatchdogModel> watchdogs = configModel.get().watchdogs;
            if(watchdogs != null) {
                FabricConfig.WatchdogModel model = watchdogs.get("tps");
                return this.getTPS() >= model.startOn;
            }
        }
        return false;
    }

    @Override
    public void stop() {

    }

    private void saveTickTime(MinecraftServer server) {
        if(lastTick == -1) {
            lastTick = System.nanoTime() - 50_000_000; //50_000_000 = 1/20th of a second in nanoseconds
        }
        TICK_TIMES[TICK_COUNT % TICK_TIMES.length] = System.nanoTime() - lastTick;
        lastTick = System.nanoTime();

        TICK_COUNT += 1;
    }

    private double getTPS() {
        double tps = 20;
        OptionalDouble averageTickTime = Arrays.stream(TICK_TIMES).average();
        if(averageTickTime.isPresent()) {
            double averageSec = averageTickTime.getAsDouble() / 1_000_000_000D; //Convert to seconds
            tps = 1D / averageSec;
        }
        return tps;
    }
}
