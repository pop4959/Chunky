package org.popcraft.chunky.platform.watchdog;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.ChunkyFabric;
import org.popcraft.chunky.platform.FabricConfig;

import java.util.Map;
import java.util.Optional;

public class FabricPlayerWatchdog extends PlayerWatchdog {

    private ChunkyFabric chunky;
    private int playerCount;

    public FabricPlayerWatchdog(ChunkyFabric chunky) {
        this.chunky = chunky;
        ServerTickEvents.START_SERVER_TICK.register(t -> {
            //This isn't too expensive so it's probably fine to do every tick
            playerCount = t.getCurrentPlayerCount();
        });
    }

    @Override
    public boolean allowsGenerationRun() {
        Optional<FabricConfig.ConfigModel> configModel = ((FabricConfig) chunky.getChunky().getConfig()).getConfigModel();
        if(configModel.isPresent()) {
            Map<String, FabricConfig.WatchdogModel> watchdogs = configModel.get().watchdogs;
            if(watchdogs != null) {
                FabricConfig.WatchdogModel model = watchdogs.get("players");
                return model.startOn >= playerCount;
            }
        }
        return false;
    }

    @Override
    public void stop() {

    }
}
