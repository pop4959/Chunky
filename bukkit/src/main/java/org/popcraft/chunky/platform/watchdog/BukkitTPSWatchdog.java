package org.popcraft.chunky.platform.watchdog;

import org.bukkit.scheduler.BukkitTask;
import org.popcraft.chunky.ChunkyBukkit;
import org.popcraft.chunky.watchdog.CommonTpsService;

public class BukkitTPSWatchdog extends TPSWatchdog {
    private BukkitTask task;
    private CommonTpsService tpsService;
    private ChunkyBukkit chunky;

    public BukkitTPSWatchdog(ChunkyBukkit chunky, CommonTpsService tpsService) {
        this.chunky = chunky;
        this.tpsService = tpsService;
    }

    @Override
    public boolean allowsGenerationRun() {
        return tpsService.getTPS() >= chunky.getChunky().getConfig().getWatchdogStartOn("tps");
    }

    @Override
    public void stopInternal() {
        task.cancel();
    }

    @Override
    public void startInternal() {
        task = chunky.getServer().getScheduler().runTaskTimer(chunky, tpsService::saveTickTime, 0, 1);
    }

}
