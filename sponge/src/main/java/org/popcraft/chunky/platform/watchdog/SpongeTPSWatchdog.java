package org.popcraft.chunky.platform.watchdog;

import org.popcraft.chunky.ChunkySponge;
import org.popcraft.chunky.watchdog.CommonTpsService;
import org.spongepowered.api.scheduler.Task;

public class SpongeTPSWatchdog extends TPSWatchdog {

    private CommonTpsService tpsService;
    private Task task;
    private ChunkySponge chunky;

    public SpongeTPSWatchdog(ChunkySponge chunky, CommonTpsService tpsService) {
        this.chunky = chunky;
        this.tpsService = tpsService;
        this.task = Task.builder()
                .execute(tpsService::saveTickTime)
                .intervalTicks(1)
                .name("Chunky - Save TPS")
                .submit(chunky);
    }

    @Override
    public boolean allowsGenerationRun() {
        return tpsService.getTPS() >= chunky.getChunky().getConfig().getWatchdogStartOn("tps");
    }

    @Override
    public void stop() {
        if(task != null) {
            task.cancel();
        }
    }
}
