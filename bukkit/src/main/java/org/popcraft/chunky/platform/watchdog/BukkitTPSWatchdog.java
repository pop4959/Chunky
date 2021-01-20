package org.popcraft.chunky.platform.watchdog;

import org.bukkit.scheduler.BukkitTask;
import org.popcraft.chunky.ChunkyBukkit;
import org.popcraft.chunky.watchdog.CommonTpsService;

public class BukkitTPSWatchdog extends TPSWatchdog {
    private BukkitTask task;
    private CommonTpsService tpsService;

    public BukkitTPSWatchdog(ChunkyBukkit chunky, CommonTpsService tpsService) {
        super(chunky.getChunky());
        this.tpsService = tpsService;
        task = chunky.getServer().getScheduler().runTaskTimer(chunky, tpsService::saveTickTime, 0, 1);
    }

    @Override
    public boolean allowsGenerationRun() {
        return tpsService.getTPS() >= super.getConfiguredTPS();
    }

    @Override
    public void stop() {
        task.cancel();
    }

}
